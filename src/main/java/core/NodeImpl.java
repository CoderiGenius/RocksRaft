package core;

import com.alipay.remoting.NamedThreadFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import config.LogManagerOptions;
import config.LogStorageOptions;
import config.ReplicatorOptions;
import entity.*;
import exceptions.LogExceptionHandler;
import exceptions.LogStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.*;
import service.ElectionService;
import storage.LogStorage;
import utils.DisruptorBuilder;
import utils.Requires;
import utils.TimerManager;
import utils.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/3/25 14:09
 */

public class NodeImpl implements Node {

    public static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);

    /**
     * 表示节点状态的enum
     * leader follower领导者跟随者
     * candidate 正式选举
     * preCandidate 预选举
     */
    public enum NodeState {leader, follower, candidate, preCandidate, NODE_STATE}

    private static final NodeImpl NODE_IMPLE = new NodeImpl();

    private NodeImpl() {
    }

    /**
     * ReplicatorStateListeners
     */
    private final CopyOnWriteArrayList<ReplicatorStateListener> replicatorStateListeners = new CopyOnWriteArrayList<>();

    private final Lock reentrantLock = new ReentrantLock(true);
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Lock writeLock = this.readWriteLock.writeLock();
    protected final Lock readLock = this.readWriteLock.readLock();
    private List<PeerId> peerIdList = new CopyOnWriteArrayList<>();
    private Map<Endpoint, RpcServices> rpcServicesMap = new ConcurrentHashMap<>();
    private Map<String, PeerId> peerIdConcurrentHashMap = new ConcurrentHashMap<>();
    private Map<Endpoint, TaskRpcServices> taskRpcServices = new ConcurrentHashMap<>();
    private Map<Endpoint, Replicator> replicatorMap = new ConcurrentHashMap<>();
    private Map<Long, BallotBox> ballotBoxConcurrentHashMap = new ConcurrentHashMap<>();
    private ReplicatorGroup replicatorGroup;
    private NodeState nodeState;
    private AtomicLong lastLogTerm = new AtomicLong(0);
    private AtomicLong lastLogIndex = new AtomicLong(0);
    private AtomicLong stableLogIndex = new AtomicLong(0);
    private PeerId currentLeaderId;
    private volatile long lastVoteTerm;
    private volatile long lastPreVoteTerm;
    private Endpoint currentEndPoint;
    private String currentId;
    private Options options;
    private FSMCaller fsmCaller;
    private LogStorage logStorage;
    private TimerManager followerTimerManager;
    private EnClosureRpcRequest enClosureRpcRequest;

    // Max retry times when applying tasks.
    private static final int                                               MAX_APPLY_RETRY_TIMES    = 3;
    /**
     * Disruptor to run node service
     */
    private Disruptor<LogEntryEvent> applyDisruptor;
    private RingBuffer<LogEntryEvent> applyQueue;

    /**
     * Disruptor to run follower service
     */
    private Disruptor<LogEntryEvent> followerDisruptor;
    private RingBuffer<LogEntryEvent> followerQueue;

    /**
     * Current node entity, including peedId inside
     */
    private NodeId nodeId;
    private NodeId leaderId;

    private Ballot preVoteBallot;
    private Ballot electionBallot;
    private Heartbeat heartbeat;
    private LogManager logManager;
    private StateMachine stateMachine;
    private ScheduledFuture scheduledFuture;
    private Runnable runnable;


    /**
     * 上一次收到心跳包的时间
     */
    private AtomicLong lastReceiveHeartbeatTime = new AtomicLong(0);


    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public Map<Endpoint, TaskRpcServices> getTaskRpcServices() {
        return taskRpcServices;
    }

    public void setTaskRpcServices(Map<Endpoint, TaskRpcServices> taskRpcServices) {
        this.taskRpcServices = taskRpcServices;
    }

    private static class LogEntryEventFactory implements EventFactory<LogEntryEvent> {

        @Override
        public LogEntryEvent newInstance() {
            return new LogEntryEvent();
        }
    }

    @Override
    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public void startToPerformAsLeader() {
        if (NodeState.leader.equals(NodeImpl.getNodeImple().getNodeState())) {
            return;
        }
        NodeImpl.getNodeImple().getWriteLock().lock();
        try {
            NodeImpl.getNodeImple().setNodeState(NodeState.leader);
            //generate replicator for all followers
            for (PeerId peerId : getPeerIdList()) {
                if ( ! peerId.equals(getNodeId().getPeerId())) {

                    ReplicatorOptions replicatorOptions = new ReplicatorOptions(
                            getOptions().getCurrentNodeOptions().getMaxHeartBeatTime(),
                            getOptions().getCurrentNodeOptions().getElectionTimeOut()
                            , getNodeId().getGroupId(), peerId, getLogManager()
                            , getNodeImple(), getLastLogTerm().longValue()
                            , new TimerManager(), ReplicatorType.Follower);
                    Replicator replicator = new Replicator(replicatorOptions,
                            rpcServicesMap.get(peerId.getEndpoint()));

                    replicatorMap.put(peerId.getEndpoint(), replicator);
                    getReplicatorGroup().addReplicator(peerId, replicator);
                }
            }
            LOG.info("Start to work as leader at term: {}",getLastLogTerm());

        } catch (Exception e) {
            LOG.error("startToPerformAsLeader error {}",e.getMessage());
        } finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }

    }

    @Override
    public boolean init() {
        Requires.requireNonNull(getOptions(), "Null node options");

        //set node state
        setNodeState(NodeState.follower);
        this.applyDisruptor = DisruptorBuilder.<LogEntryEvent>newInstance()
                .setRingBufferSize(NodeOptions.getNodeOptions().getDisruptorBufferSize())
                .setEventFactory(new LogEntryEventFactory())
                .setThreadFactory(new NamedThreadFactory("JRaft-NodeImpl-Disruptor-", true))
                .setProducerType(ProducerType.MULTI)
                .setWaitStrategy(new BlockingWaitStrategy())
                .build();
        this.applyDisruptor.handleEventsWith(new LogEntryEventHandler());
        this.applyDisruptor.setDefaultExceptionHandler(
                new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.applyQueue = this.applyDisruptor.start();
        this.replicatorGroup = new ReplicatorGroupImpl();
        ReplicatorGroupOptions replicatorGroupOptions =
                new ReplicatorGroupOptions(
                        getOptions().getCurrentNodeOptions().getMaxHeartBeatTime(),
                        getOptions().getCurrentNodeOptions().getElectionTimeOut(),
                        getLogManager());
        this.replicatorGroup.init(getNodeId(),replicatorGroupOptions);

        this.followerDisruptor = DisruptorBuilder.<LogEntryEvent>newInstance()
                .setRingBufferSize(NodeOptions.getNodeOptions().getDisruptorBufferSize())
                .setEventFactory(new LogEntryEventFactory())
                .setThreadFactory(new NamedThreadFactory("JRaft-NodeImpl-Disruptor-", true))
                .setProducerType(ProducerType.MULTI)
                .setWaitStrategy(new BlockingWaitStrategy())
                .build();
        this.followerDisruptor.handleEventsWith(new LogEntryEventHandlerForFollower());
        this.followerDisruptor.setDefaultExceptionHandler(
                new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.followerQueue = this.followerDisruptor.start();

        //init FSMCaller
        this.fsmCaller = new FSMCallerImpl();
        final FSMCallerOptions fsmCallerOptions = new FSMCallerOptions();
        //fsmCallerOptions.setAfterShutdown(status -> afterShutdown());
        fsmCallerOptions.setLogManager(this.logManager);
        fsmCallerOptions.setFsm(getStateMachine());
        fsmCallerOptions.setNode(NodeImpl.getNodeImple());
        fsmCallerOptions.setBootstrapId(new LogId(0, 0));
        getFsmCaller().init(fsmCallerOptions);
        currentEndPoint = new Endpoint(
                getNodeId().getPeerId().getEndpoint().getIp(),
                getNodeId().getPeerId().getEndpoint().getPort());
        LOG.info("Node init finished successfully");

        //LogStorage
        LogStorageOptions logStorageOptions =
                new LogStorageOptions(getOptions().getCurrentNodeOptions().getLogStoragePath(),
                        getOptions().getCurrentNodeOptions().getLogStorageName());
        this.logStorage = new LogStorageImpl(logStorageOptions);
        //logStorage.init();

        //Logmanager
        LogManagerOptions logManagerOptions = new LogManagerOptions(logStorage,getOptions());
        this.logManager = new LogManagerImpl();
        try {
            logManager.init(logManagerOptions);
        } catch (LogStorageException e) {
            e.printStackTrace();
            LOG.error("Raft logManager error {}",e.getErrMsg());
        }
        setFollowerTimerManager(new TimerManager());
        getFollowerTimerManager().init(100);
        this.runnable = () -> {
            if ((Utils.monotonicMs()
                    - NodeImpl.getNodeImple().getLastReceiveHeartbeatTime().longValue())
                    >= getOptions().getCurrentNodeOptions().getMaxHeartBeatTime()) {
                //超时，执行超时逻辑
                LOG.error("Node timeout, start to launch TimeOut actions");
                //timeOutClosure.run(null);
                ElectionService.checkToStartPreVote();
            }
        };
        setChecker();

        setEnClosureRpcRequest(new EnClosureRpcRequest(getRpcServicesMap()));
        return true;
    }

    public boolean checkIfLeaderChanged(String peeId) {
        if (getLeaderId() == null) {
            return true;
        }

        if(getLeaderId().getPeerId().getId().equals(peeId)) {
            return false;
        }else {
            return true;
        }
    }

    public boolean setChecker() {
        LOG.debug("SetChecker");
        setScheduledFuture(getFollowerTimerManager().schedule(getRunnable(),
                getOptions().getCurrentNodeOptions().getMaxHeartBeatTime(),
                TimeUnit.MILLISECONDS));
        return true;
    }

    @Override
    public List<ReplicatorStateListener> getReplicatorStatueListeners() {
        return this.replicatorStateListeners;
    }

    /**
     * Called by follower when receive prob request from new leader
     * @param request
     * @return
     */
    @Override
    public boolean transformLeader(RpcRequests.AppendEntriesRequest request) {
        try {


            LOG.info("Start to transform Leader from {} to {}"
                    , getLeaderId(), request.getPeerId());
            getWriteLock().lock();
            try {
                //check if leader is valid
                if (!checkNodeAheadOfCurrent(request.getTerm(), request.getCommittedIndex())) {
                    LOG.error("leader is invalid with term {} index {} while current term {} index {}"
                            , request.getTerm(), request.getCommittedIndex()
                            , getLastLogTerm(), getLastLogIndex());
                    return false;
                }

                setNodeState(NodeState.follower);
                PeerId peerId = new PeerId(request.getPeerId(), request.getPeerId()
                        , request.getAddress(), request.getPort(), request.getTaskPort());
                NodeId nodeId = new NodeId(request.getGroupId(), peerId);
                setLeaderId(nodeId);
                getLastLogTerm().set(request.getTerm());
                LOG.debug("transform success current state:{} current term {}",getNodeState(),getLastLogTerm());
                return true;
            } catch (Exception e) {
                LOG.error("Transform leader error:{}", e.getMessage());
            } finally {
                getWriteLock().unlock();
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void apply(Task task) {
        Requires.requireNonNull(task, "Null task");
        LOG.info("Applying task");

        if (NodeState.follower.equals(getNodeState())) {
            LOG.info("Current node is in follower state, leader is {} forwarding request……"
                    , getLeaderId().getPeerId().getPeerName());
            getTaskRpcServices().get(getLeaderId().getPeerId().getEndpoint()).apply(task);
        } else if (!NodeState.leader.equals(getNodeState())) {
            LOG.info("Current node is not in valid state {}", getNodeState());
            Utils.runClosureInThread(task.getDone()
                    , new Status(RaftError.ENODESHUTDOWN, "Current node is not in valid state {}", getNodeState()));

            LogEntry logEntry = new LogEntry();
            logEntry.setData(task.getData());
            logEntry.setLeaderId(getLeaderId().getPeerId());
            int retryTimes = 0;
            try {
                final EventTranslator<LogEntryEvent> translator = (event, sequence) -> {
                    event.reset();
                    event.entry = logEntry;
                    event.done = task.getDone();
                    event.expectedTerm = task.getExpectedTerm();
                };

                while (true) {
                    if (this.applyQueue.tryPublishEvent(translator)) {
                        break;
                    } else {
                        retryTimes++;
                        if (retryTimes > MAX_APPLY_RETRY_TIMES) {
                            Utils.runClosureInThread(task.getDone(),
                                    new Status(RaftError.EBUSY, "Node is busy, has too many tasks."));
                            LOG.warn("Node {} applyQueue is overload.", getNodeId());
                            return;
                        }
                        Thread.yield();
                    }
                }
            } catch (Exception e) {
                Utils.runClosureInThread(task.getDone(), new Status(RaftError.EPERM, "Node is down."));
                LOG.info("New Translator error {}", e.getMessage());
            }
        }
    }


    private class LogEntryEventHandler implements EventHandler<LogEntryEvent> {
        // task list for batch
        private final List<LogEntryEvent> tasks =
                new ArrayList<LogEntryEvent>(NodeOptions.getNodeOptions().getApplyBatch());

        @Override
        public void onEvent(LogEntryEvent logEntryEvent, long l, boolean endOfBatch) throws Exception {
            this.tasks.add(logEntryEvent);
            if (this.tasks.size() >= NodeOptions.getNodeOptions().getApplyBatch() || endOfBatch) {
                executeApplyingTasks(this.tasks);
                this.tasks.clear();
            }
        }
    }

    private class LogEntryEventHandlerForFollower implements EventHandler<LogEntryEvent> {
        // task list for batch
        private final List<LogEntryEvent> tasks =
                new ArrayList<LogEntryEvent>(NodeOptions.getNodeOptions().getApplyBatch());

        @Override
        public void onEvent(LogEntryEvent logEntryEvent, long l, boolean endOfBatch) throws Exception {
            this.tasks.add(logEntryEvent);
            if (this.tasks.size() >= NodeOptions.getNodeOptions().getApplyBatch() || endOfBatch) {
                executeFollowerTasks(tasks);
                this.tasks.clear();
            }
        }
    }

    private void executeApplyingTasks(final List<LogEntryEvent> tasks) {
        this.writeLock.lock();
        try {
            final int size = tasks.size();
            if (getNodeState().equals(NodeState.leader)) {
                final Status st = new Status();
                st.setError(RaftError.EPERM, "Is not leader.");
                LOG.warn("Not in leader state while executeApplyingTasks");
                final List<LogEntryEvent> savedTasks = new ArrayList<>(tasks);
                Utils.runInThread(() -> {
                    for (int i = 0; i < size; i++) {
                        savedTasks.get(i).done.run(st);
                    }
                });
                return;
            }
            final List<LogEntry> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                final LogEntryEvent task = tasks.get(i);
                if (task.expectedTerm != -1 && task.expectedTerm != getLastLogTerm().get()) {
                    LOG.debug("Node {} can't apply task whose expectedTerm={} doesn't match currTerm={}.", getNodeId(),
                            task.expectedTerm, this.getLastLogTerm());
                    if (task.done != null) {
                        final Status st = new Status(RaftError.EPERM, "expected_term=%d doesn't match current_term=%d",
                                task.expectedTerm, this.getLastLogTerm());
                        Utils.runClosureInThread(task.done, st);
                    }
                    continue;
                }
                // set task entry info before adding to list.
                task.entry.getId().setIndex(this.lastLogIndex.getAndIncrement());
                task.entry.getId().setTerm(this.getLastLogTerm().get());

                task.entry.setType(EnumOutter.EntryType.ENTRY_TYPE_DATA);
                entries.add(task.entry);
            }
            this.logManager.appendEntries(entries, new LeaderStableClosure(entries));
        } catch (Exception e) {
            LOG.error("executeApplyingTasks failed {}",e.getMessage());
        }finally {
            getWriteLock().unlock();
        }
    }
    private void executeFollowerTasks(final List<LogEntryEvent> tasks) {
        this.writeLock.lock();
        try {
            final int size = tasks.size();

            final List<LogEntry> entries = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                final LogEntryEvent task = tasks.get(i);
                if (task.expectedTerm != -1 && task.expectedTerm != getLastLogTerm().get()) {
                    LOG.debug("Node {} can't apply task whose expectedTerm={} doesn't match currTerm={}.", getNodeId(),
                            task.expectedTerm, this.getLastLogTerm());
                    if (task.done != null) {
                        final Status st = new Status(RaftError.EPERM, "expected_term=%d doesn't match current_term=%d",
                                task.expectedTerm, this.getLastLogTerm());
                        Utils.runClosureInThread(task.done, st);
                    }
                    continue;
                }
                // set task entry info before adding to list.
                task.entry.getId().setIndex(this.lastLogIndex.getAndIncrement());
                task.entry.getId().setTerm(this.getLastLogTerm().get());

                task.entry.setType(EnumOutter.EntryType.ENTRY_TYPE_DATA);
                entries.add(task.entry);
            }
            this.logManager.appendEntries(entries, new FollowerStableClosure(entries));
        } catch (Exception e) {
            LOG.error("executeApplyingTasks failed {}",e.getMessage());
        }finally {
            getWriteLock().unlock();
        }
    }
    /**
     * Come from follower, leader invokes this method to handle follower stable event
     * @param notifyFollowerStableRequest
     * @return
     */
    public boolean handleFollowerStableEvent(RpcRequests.NotifyFollowerStableRequest notifyFollowerStableRequest) {

        getBallotBoxConcurrentHashMap()
                .get(notifyFollowerStableRequest.getFirstIndex())
                .checkGranted(notifyFollowerStableRequest.getPeerId()
                        ,notifyFollowerStableRequest.getFirstIndex()
                        ,notifyFollowerStableRequest.getLastIndex()-notifyFollowerStableRequest.getFirstIndex());
    return true;
    }

    /**
     * Invoked by followers when received appendEntries from leader
     * @param appendEntriesRequest
     * @return
     */
    public boolean followerSetLogEvent
    (RpcRequests.AppendEntriesRequest appendEntriesRequest) {
       LogEntry logEntry = new LogEntry();
       logEntry.setData(ByteBuffer.wrap(appendEntriesRequest.getData().toByteArray()));
        final EventTranslator<LogEntryEvent> translator = (event, sequence) -> {
            event.reset();
            event.entry = logEntry;
            //event.done = task.getDone();
            event.expectedTerm = appendEntriesRequest.getTerm();
        };
        int retryTimes = 0;
        while (true) {
            if (this.followerQueue.tryPublishEvent(translator)) {
                break;
            } else {
                retryTimes++;
                if (retryTimes > MAX_APPLY_RETRY_TIMES) {
                    LOG.warn("Node {} applyQueue is overload.", getNodeId());
                    return false;
                }
                Thread.yield();
            }
    }
        return true;
    }

    class LeaderStableClosure extends LogManager.StableClosure {

        public LeaderStableClosure(final List<LogEntry> entries) {
            super(entries);
        }

        @Override
        public void run(final Status status) {
            if (status.isOk()) {
                BallotBox ballotBox = new BallotBox(getPeerIdList(),
                        this.firstLogIndex, this.nEntries);
                ballotBox.grant(getLeaderId().getPeerId().getId());
                getBallotBoxConcurrentHashMap().put(this.firstLogIndex,ballotBox);
            } else {
                LOG.error("Node {} append [{}, {}] failed, status={}.", getNodeId(), this.firstLogIndex,
                        this.firstLogIndex + this.nEntries - 1, status);
            }
        }
    }



    public void handleAppendEntriesResponse(final RpcRequests.AppendEntriesResponse appendEntriesResponse) {

        LOG.debug("handleAppendEntriesResponse from {} at term {}",
                appendEntriesResponse.getPeerId(),appendEntriesResponse.getTerm());

       if (!appendEntriesResponse.getSuccess()) {
           //log rePlay at the given position
           NodeImpl.getNodeImple()
                   .getReplicatorGroup().sendInflight(
                   appendEntriesResponse.getAddress(),
                   appendEntriesResponse.getPort(),
                   appendEntriesResponse.getLastLogIndex());
       }else {
           NodeImpl.getNodeImple()
                   .getBallotBoxConcurrentHashMap()
                   .get(appendEntriesResponse.getLastLogIndex())
                   .grant(appendEntriesResponse.getPeerId());
       }
       return;
    }

    class FollowerStableClosure extends LogManager.StableClosure {

        public FollowerStableClosure(final List<LogEntry> entries) {
            super(entries);
        }

        @Override
        public void run(final Status status) {
            if (status.isOk()) {
            //Notify leader through RPC

                RpcRequests.NotifyFollowerStableRequest.Builder builder
                        = RpcRequests.NotifyFollowerStableRequest.newBuilder();
                builder.setFirstIndex(status.getFirstIndex());
                builder.setLastIndex(status.getLastIndex());
                builder.setPeerId(getCurrentId());
//            getRpcServicesMap().get(getLeaderId().getPeerId().getEndpoint())
//                    .handleFollowerStableRequest(builder.build());
                NodeImpl.getNodeImple().getEnClosureRpcRequest()
                        .handleFollowerStableRequest(builder.build(),
                                getLeaderId().getPeerId().getEndpoint(),true);
            } else {
                LOG.error("Node {} append [{}, {}] failed, status={}.", getNodeId(), this.firstLogIndex,
                        this.firstLogIndex + this.nEntries - 1, status);
            }
        }
    }

    public long getLastVoteTerm() {
        return lastVoteTerm;
    }

    public void setLastVoteTerm(long lastVoteTerm) {
        this.lastVoteTerm = lastVoteTerm;
    }

    public long getLastPreVoteTerm() {
        return lastPreVoteTerm;
    }

    public void setLastPreVoteTerm(long lastPreVoteTerm) {
        this.lastPreVoteTerm = lastPreVoteTerm;
    }

    public boolean checkIfCurrentNodeCanVoteOthers() {
        //only leader, follower, preCandidate, NODE_STATE can vote others
        if (NodeState.preCandidate.equals(getNodeState())
                || NodeState.follower.equals(getNodeState())
                || NodeState.leader.equals(getNodeState())
                || NodeState.NODE_STATE.equals(getNodeState())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkNodeAheadOfCurrent(long term, long index) {
        return term >= getLastLogTerm().longValue() && index >= getLastLogIndex().longValue();
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public boolean checkIfCurrentNodeCanStartPreVote() {
        //only  follower,None_state  can start PreVote
        if (NodeState.NODE_STATE.equals(getNodeState())
                || NodeState.follower.equals(getNodeState())
        ) {
            return true;
        } else {
            return false;
        }
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public ScheduledFuture getScheduledFuture() {
        return scheduledFuture;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public boolean checkIfCurrentNodeCanStartElection() {
        //only  PreVote can start PreVote
        if (NodeState.preCandidate.equals(NodeImpl.getNodeImple().getNodeState())
        ) {
            return true;
        } else {
            return false;
        }
    }

    public Endpoint getCurrentEndPoint() {
        return currentEndPoint;
    }
    public TimerManager getFollowerTimerManager() {
        return followerTimerManager;
    }

    public void setFollowerTimerManager(TimerManager followerTimerManager) {
        this.followerTimerManager = followerTimerManager;
    }
    public void setCurrentEndPoint(Endpoint currentEndPoint) {
        this.currentEndPoint = currentEndPoint;
    }

    public Ballot getPreVoteBallot() {
        return preVoteBallot;
    }

    public void setPreVoteBallot(Ballot preVoteBallot) {
        this.preVoteBallot = preVoteBallot;
    }


    public Map<Endpoint, RpcServices> getRpcServicesMap() {
        return rpcServicesMap;
    }

    public Ballot getElectionBallot() {
        return electionBallot;
    }

    public void setElectionBallot(Ballot electionBallot) {
        this.electionBallot = electionBallot;
    }

    public void setRpcServicesMap(Map<Endpoint, RpcServices> rpcServicesMap) {
        this.rpcServicesMap = rpcServicesMap;
    }


    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public AtomicLong getLastReceiveHeartbeatTime() {
        return lastReceiveHeartbeatTime;
    }

    public void setLastReceiveHeartbeatTime(long lastReceiveHeartbeatTime) {

        getLastReceiveHeartbeatTime().set(lastReceiveHeartbeatTime);
    }

    public static NodeImpl getNodeImple() {
        return NODE_IMPLE;
    }

    public AtomicLong getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(AtomicLong lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    public AtomicLong getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(AtomicLong lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public List<PeerId> getPeerIdList() {
        return peerIdList;
    }

    public Map<Endpoint, Replicator> getReplicatorMap() {
        return replicatorMap;
    }

    public void setReplicatorMap(Map<Endpoint, Replicator> replicatorMap) {
        this.replicatorMap = replicatorMap;
    }

    public ReplicatorGroup getReplicatorGroup() {
        return replicatorGroup;
    }

    public void setReplicatorGroup(ReplicatorGroup replicatorGroup) {
        this.replicatorGroup = replicatorGroup;
    }

    public void setPeerIdList(List<PeerId> peerIdList) {
        this.peerIdList = peerIdList;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    public NodeState getNodeState() {
        this.readLock.lock();
        try {
            return nodeState;
        } catch (Exception e) {
            LOG.error("Read nodeState error {}",e.getMessage());
        }finally {
            this.readLock.unlock();
        }
        return null;
    }

    public Map<String, PeerId> getPeerIdConcurrentHashMap() {
        return peerIdConcurrentHashMap;
    }

    public void setPeerIdConcurrentHashMap(Map<String, PeerId> peerIdConcurrentHashMap) {
        this.peerIdConcurrentHashMap = peerIdConcurrentHashMap;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public boolean checkNodeStateCandidate() {
        return NodeState.candidate.equals(getNodeState());
    }

    public boolean checkNodeStatePreCandidate() {
        return NodeState.preCandidate.equals(getNodeState());
    }

    public boolean isCurrentLeaderValid() {
        return Utils.monotonicMs() - this.lastReceiveHeartbeatTime.get() < NodeOptions.getNodeOptions().getMaxHeartBeatTime();
    }

    public Map<Long, BallotBox> getBallotBoxConcurrentHashMap() {
        return ballotBoxConcurrentHashMap;
    }

    public void setBallotBoxConcurrentHashMap(Map<Long, BallotBox> ballotBoxConcurrentHashMap) {
        this.ballotBoxConcurrentHashMap = ballotBoxConcurrentHashMap;
    }

    public AtomicLong getStableLogIndex() {
        return stableLogIndex;
    }

    public void setStableLogIndex(AtomicLong stableLogIndex) {
        this.stableLogIndex = stableLogIndex;
    }

    public Disruptor<LogEntryEvent> getApplyDisruptor() {
        return applyDisruptor;
    }

    public void setApplyDisruptor(Disruptor<LogEntryEvent> applyDisruptor) {
        this.applyDisruptor = applyDisruptor;
    }

    public RingBuffer<LogEntryEvent> getApplyQueue() {
        return applyQueue;
    }

    public void setApplyQueue(RingBuffer<LogEntryEvent> applyQueue) {
        this.applyQueue = applyQueue;
    }

    public Disruptor<LogEntryEvent> getFollowerDisruptor() {
        return followerDisruptor;
    }

    public void setFollowerDisruptor(Disruptor<LogEntryEvent> followerDisruptor) {
        this.followerDisruptor = followerDisruptor;
    }

    public RingBuffer<LogEntryEvent> getFollowerQueue() {
        return followerQueue;
    }

    public PeerId getCurrentLeaderId() {
        return currentLeaderId;
    }

    public void setCurrentLeaderId(PeerId currentLeaderId) {
        this.currentLeaderId = currentLeaderId;
    }

    public String getCurrentId() {
        return currentId;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }

    public void setFollowerQueue(RingBuffer<LogEntryEvent> followerQueue) {
        this.followerQueue = followerQueue;
    }

    public FSMCaller getFsmCaller() {
        return fsmCaller;
    }

    public void setFsmCaller(FSMCaller fsmCaller) {
        this.fsmCaller = fsmCaller;
    }

    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    public EnClosureRpcRequest getEnClosureRpcRequest() {
        return enClosureRpcRequest;
    }

    public void setEnClosureRpcRequest(EnClosureRpcRequest enClosureRpcRequest) {
        this.enClosureRpcRequest = enClosureRpcRequest;
    }
}

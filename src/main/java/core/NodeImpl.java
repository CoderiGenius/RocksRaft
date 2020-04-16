package core;

import com.alipay.remoting.NamedThreadFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import config.ReplicatorOptions;
import entity.*;
import exceptions.LogExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.EnumOutter;
import rpc.RpcRequests;
import rpc.RpcServices;
import rpc.TaskRpcServices;
import utils.DisruptorBuilder;
import utils.Requires;
import utils.TimerManager;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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
    public enum NodeState {leader, follwoer, candidate, preCandidate, NODE_STATE}

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
    private Map<Endpoint, TaskRpcServices> taskRpcServices = new ConcurrentHashMap<>();
    private Map<Endpoint, Replicator> replicatorMap = new ConcurrentHashMap<>();
    private ReplicatorGroup replicatorGroup;
    private NodeState nodeState;
    private AtomicLong lastLogTerm = new AtomicLong(0);
    private AtomicLong lastLogIndex = new AtomicLong(0);
    private PeerId currentLeaderId;
    private Options options;

    // Max retry times when applying tasks.
    private static final int                                               MAX_APPLY_RETRY_TIMES    = 3;
    /**
     * Disruptor to run node service
     */
    private Disruptor<LogEntryEvent> applyDisruptor;
    private RingBuffer<LogEntryEvent> applyQueue;

    /**
     * Current node entity, including peedId inside
     */
    private NodeId nodeId;
    private NodeId leaderId;

    private Ballot preVoteBallot;
    private Ballot electionBallot;
    private Heartbeat heartbeat;
    private LogManager logManager;


    /**
     * 上一次收到心跳包的时间
     */
    private AtomicLong lastReceiveHeartbeatTime;


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

                ReplicatorOptions replicatorOptions = new ReplicatorOptions(
                        getOptions().getCurrentNodeOptions().getMaxHeartBeatTime(),
                        getOptions().getCurrentNodeOptions().getElectionTimeOut()
                        , getNodeId().getGroupId(), getNodeId().getPeerId(), getLogManager()
                        , new BallotBox(getPeerIdList()), getNodeImple(), getLastLogTerm().longValue()
                        , new TimerManager(), ReplicatorType.Follower);
                Replicator replicator = new Replicator(replicatorOptions, rpcServicesMap.get(peerId.getEndpoint()));

                replicatorMap.put(peerId.getEndpoint(), replicator);

            }

        } catch (Exception e) {

        } finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }

    }

    @Override
    public boolean init() {
        Requires.requireNonNull(getOptions(), "Null node options");

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
        return true;
    }

    @Override
    public List<ReplicatorStateListener> getReplicatorStatueListeners() {
        return this.replicatorStateListeners;
    }

    @Override
    public boolean transformLeader(RpcRequests.AppendEntriesRequest request) {
        LOG.info("Start to transform Leader from {} to {}"
                , getLeaderId().getPeerId().getPeerName(), request.getPeerId());
        getWriteLock().lock();
        try {
            //check if leader is valid
            if (!checkNodeAheadOfCurrent(request.getTerm(), request.getCommittedIndex())) {
                LOG.error("leader is invalid with term {} index {} while current term {} index {}"
                        , request.getTerm(), request.getCommittedIndex()
                        , getLastLogTerm(), getLastLogIndex());
                return false;
            }

            setNodeState(NodeState.follwoer);
            PeerId peerId = new PeerId(request.getPeerId(), request.getPeerId()
                    , request.getAddress(), request.getPort(), request.getTaskPort());
            NodeId nodeId = new NodeId(request.getGroupId(), peerId);
            setLeaderId(nodeId);
            ReplicatorGroupOptions replicatorGroupOptions = new ReplicatorGroupOptions();
            //there is no need to set properties for replicatorGroupOptions as we get options directly
            this.replicatorGroup.init(getNodeId(),replicatorGroupOptions);
            for (PeerId p:peerIdList
                 ) {
                ReplicatorOptions replicatorOptions =new ReplicatorOptions(p);
                Replicator replicator = new Replicator(replicatorOptions,getRpcServicesMap().get(p.getEndpoint()));
                getReplicatorGroup().addReplicator(p,replicator);
            }
            return true;
        } catch (Exception e) {
            LOG.error("Transform leader error:{}", e.getMessage());
        } finally {
            getWriteLock().unlock();
        }
        return false;
    }

    @Override
    public void apply(Task task) {
        Requires.requireNonNull(task, "Null task");
        LOG.info("Applying task");

        if (NodeState.follwoer.equals(getNodeState())) {
            LOG.info("Current node is in follower state, leader is {} forwarding request……"
                    , getLeaderId().getPeerId().getPeerName());
            getTaskRpcServices().get(getLeaderId().getPeerId().getEndpoint()).apply(task);
        } else if (!NodeState.leader.equals(getNodeState())) {
            LOG.info("Current node is not in valid state {}", getNodeState());
            Utils.runClosureInThread(task.getDone()
                    , new Status(RaftError.ENODESHUTDOWN, "Current node is not in valid state {}", getNodeState()));

            LogEntry logEntry = new LogEntry();
            logEntry.setData(task.getData());
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

    class LeaderStableClosure extends LogManager.StableClosure {

        public LeaderStableClosure(final List<LogEntry> entries) {
            super(entries);
        }

        @Override
        public void run(final Status status) {
            if (status.isOk()) {
                NodeImpl.this.ballotBox.commitAt(this.firstLogIndex, this.firstLogIndex + this.nEntries - 1,
                        NodeImpl.this.serverId);
            } else {
                LOG.error("Node {} append [{}, {}] failed, status={}.", getNodeId(), this.firstLogIndex,
                        this.firstLogIndex + this.nEntries - 1, status);
            }
        }
    }

    public boolean checkIfCurrentNodeCanVoteOthers() {
        //only leader, follower, preCandidate, NODE_STATE can vote others
        if (NodeState.preCandidate.equals(getNodeState())
                || NodeState.follwoer.equals(getNodeState())
                || NodeState.leader.equals(getNodeState())
                || NodeState.NODE_STATE.equals(getNodeState())) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkNodeAheadOfCurrent(long term, long index) {
        return term > getLastLogTerm().longValue() && index > getLastLogIndex().longValue();
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public boolean checkIfCurrentNodeCanStartPreVote() {
        //only  follower,None_state  can start PreVote
        if (NodeState.NODE_STATE.equals(getNodeState())
                || NodeState.follwoer.equals(getNodeState())
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

    public void setLastReceiveHeartbeatTime(AtomicLong lastReceiveHeartbeatTime) {

        this.lastReceiveHeartbeatTime = lastReceiveHeartbeatTime;
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
}

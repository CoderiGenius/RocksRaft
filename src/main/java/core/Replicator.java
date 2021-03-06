package core;

/**
 * Created by 周思成 on  2020/4/5 23:34
 * @author Mike
 */


import com.alipay.remoting.NamedThreadFactory;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.google.protobuf.ZeroByteStringHelper;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import config.ReplicatorOptions;
import entity.*;
import exceptions.LogExceptionHandler;
import org.apache.log4j.pattern.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcRequests;
import rpc.RpcServices;
import utils.DisruptorBuilder;
import utils.Requires;
import utils.TimerManager;
import utils.Utils;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Replicator for replicating log entry from leader to followers.
 */
public class Replicator {



    enum ReplicatorState{
        // idle
        IDLE,
        // blocking state
        BLOCKING,
        // appending log entries
        APPENDING_ENTRIES,
    }
    enum ReplicatorEvent {
        // created
        CREATED,
        // error
        ERROR,
        // destroyed
        DESTROYED
    }
    public enum State {
        // probe follower state
        Probe,
        // replicate logs normally
        Replicate,
        // destroyed
        Destroyed
    }
    static class Inflight{
        // In-flight request count
        final int             count;
        // Start log index
        final long            startIndex;
        // Entries size in bytes
        final int             size;
        // RPC future
//        final Future<Message> rpcFuture;


        // Request sequence.
//        final int             seq;
//        public Inflight( final long startIndex, final int count, final int size,
//                        final int seq) {
//            super();
//            this.seq = seq;
//            this.count = count;
//            this.startIndex = startIndex;
//            this.size = size;
//
//        }

        public Inflight(long committedIndex, int size, int serializedSize) {
            super();
            this.count = size;
            this.startIndex = committedIndex;
            this.size = serializedSize;
        }

        @Override
        public String toString() {
            return "Inflight [count=" + this.count + ", startIndex=" + this.startIndex + ", size=" + this.size
                      ;
        }

        public int getCount() {
            return count;
        }

        public long getStartIndex() {
            return startIndex;
        }

        public int getSize() {
            return size;
        }
    }


    private static final Logger LOG                    = LoggerFactory.getLogger(Replicator.class);
    private final ReplicatorOptions          options;
    private ReentrantReadWriteLock reentrantLock = new ReentrantReadWriteLock();
    private ReentrantReadWriteLock.WriteLock writeLock = reentrantLock.writeLock();
    private ReentrantReadWriteLock.ReadLock readLock = reentrantLock.readLock();
    private Endpoint endpoint;
    private TimerManager timerManager;
    private RpcServices rpcServices;
    private ReplicatorState state;
    private State followerState;

    private AtomicLong currentSuccessTerm;
    private AtomicLong currentSuccessIndex;
    private List<RpcRequests.AppendEntriesRequest> appendEntriesRequestList
            = new CopyOnWriteArrayList<>();
    private Disruptor<LogEntryEvent> disruptor;
    private RingBuffer<LogEntryEvent> ringBuffer;
    // In-flight RPC requests, FIFO queue
    private final ArrayDeque<Inflight> inflights              = new ArrayDeque<>();
    private final Map<Long,Inflight> inflightMap              = new ConcurrentHashMap<>();
    private volatile long lastInflightIndex;


    public Replicator(ReplicatorOptions options,RpcServices rpcServices) {
        this.state = ReplicatorState.IDLE;
        this.options = options;
        this.rpcServices = rpcServices;
        this.endpoint = options.getPeerId().getEndpoint();
        this.followerState = State.Probe;
        this.disruptor = DisruptorBuilder.<LogEntryEvent>newInstance()
                .setRingBufferSize(NodeOptions.getNodeOptions().getDisruptorBufferSize())
                .setEventFactory(new LogEntryEventFactory())
                .setThreadFactory(new NamedThreadFactory("JRaft-Replicator-Disruptor-", true))
                .setProducerType(ProducerType.MULTI)
                .setWaitStrategy(new BlockingWaitStrategy())
                .build();
        disruptor.handleEventsWith( new ReplicatorHandler());
        disruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        ringBuffer = disruptor.start();
        this.timerManager = new TimerManager();
        timerManager.init(10);
    }

    private  class ReplicatorHandler implements EventHandler<LogEntryEvent> {



        @Override
        public void onEvent(LogEntryEvent logEntryEvent, long l, boolean b) throws Exception {
            LogEntry logEntry = logEntryEvent.entry;
            LOG.debug("Replicator receive log event on logEntry: {} data: {} data:{}"
                    ,logEntry.getId(),logEntry.getData(),logEntry.getData());;
            if (b
                    ){

                final RpcRequests.AppendEntriesRequest.Builder builder1
                        = RpcRequests.AppendEntriesRequest.newBuilder();
                builder1.setData(ZeroByteStringHelper.wrap(logEntry.getData()));
                builder1.setCommittedIndex(logEntry.getId().getIndex());
                builder1.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());

                builder1.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
                builder1.setGroupId(NodeImpl.getNodeImple().getNodeId().getGroupId());
                builder1.setPort(NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getPort());
                builder1.setAddress(NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getAddress());
                builder1.setTaskPort(NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getTaskPort());

                RpcRequests.AppendEntriesRequest appendEntriesRequest = builder1.build();
                LOG.warn("appendEntriesRequest CHECK:index {} dataIsEmpty:{} "
                        ,appendEntriesRequest.getCommittedIndex(),appendEntriesRequest.getData().isEmpty());
                getAppendEntriesRequestList().add(appendEntriesRequest);
            if( !getAppendEntriesRequestList().isEmpty()){
                final RpcRequests.AppendEntriesRequests.Builder builder
                        = RpcRequests.AppendEntriesRequests.newBuilder();
                builder.addAllArgs(getAppendEntriesRequestList());
                RpcRequests.AppendEntriesRequests appendEntriesRequests = builder.build();
                //rpcServices.handleApendEntriesRequests(appendEntriesRequests);
                LOG.debug("Replicator send handleAppendEntriesRequests with sizes:{} to {}"
                        ,getAppendEntriesRequestList().size(),getEndpoint());

                NodeImpl.getNodeImple().getEnClosureRpcRequest()
                        .handleAppendEntriesRequests(appendEntriesRequests,rpcServices,true);
                Inflight inflight = new Inflight(
                        getAppendEntriesRequestList().get(0).getCommittedIndex(),
                        getAppendEntriesRequestList().size(),
                        appendEntriesRequests.getSerializedSize());
                getInflights().add(inflight);

                getInflightMap().put(getAppendEntriesRequestList().get(0).getCommittedIndex(),inflight);
                getAppendEntriesRequestList().clear();
                return;
            }
            }
            if(!b){
                final RpcRequests.AppendEntriesRequest.Builder builder
                        = RpcRequests.AppendEntriesRequest.newBuilder();
                builder.setData(ZeroByteStringHelper
                        .wrap(
                                logEntry.getData()));
                builder.setCommittedIndex(logEntry.getId().getIndex());
                builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
                builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
                RpcRequests.AppendEntriesRequest appendEntriesRequest = builder.build();
                LOG.warn("appendEntriesRequest CHECK2:index {} dataIsEmpty:{} "
                        ,appendEntriesRequest.getCommittedIndex(),appendEntriesRequest.getData().isEmpty());
                getAppendEntriesRequestList().add(appendEntriesRequest);
            }
        }
    }

    private void checkList(List<RpcRequests.AppendEntriesRequest> list) {
        for (RpcRequests.AppendEntriesRequest a:
             list) {
            LOG.debug("Log list check: {} dataIsEmpty: {}"
                    ,a.getCommittedIndex(),a.getData().isEmpty());
        }

    }

    private static class LogEntryEventFactory implements EventFactory<LogEntryEvent> {

        @Override
        public LogEntryEvent newInstance() {
            return new LogEntryEvent();
        }
    }
    /**
     * Notify replicator event(such as created, error, destroyed) to replicatorStateListener which is implemented by users.
     *
     * @param replicator replicator object
     * @param event      replicator's state listener event type
     * @param status     replicator's error detailed status
     */
    private static void notifyReplicatorStatusListener(final Replicator replicator, final ReplicatorEvent event,
                                                       final Status status) {
        final ReplicatorOptions replicatorOpts = (ReplicatorOptions) Requires.requireNonNull(replicator.getOpts(), "replicatorOptions");
        final Node node = Requires.requireNonNull(replicatorOpts.getNode(), "node");
        final PeerId peer = Requires.requireNonNull(replicatorOpts.getPeerId(), "peer");

        final List<ReplicatorStateListener> listenerList = node.getReplicatorStatueListeners();
        for (int i = 0; i < listenerList.size(); i++) {
            final ReplicatorStateListener listener = listenerList.get(i);
            if (listener != null) {
                try {
                    switch (event) {
                        case CREATED:
                            Utils.runInThread(() -> listener.onCreated(peer));
                            break;
                        case ERROR:
                            Utils.runInThread(() -> listener.onError(peer, status));
                            break;
                        case DESTROYED:
                            Utils.runInThread(() -> listener.onDestroyed(peer));
                            break;
                        default:
                            break;
                    }
                } catch (final Exception e) {
                    LOG.error("Fail to notify ReplicatorStatusListener, listener={}, event={}.", listener, event);
                }
            }
        }
    }
    /**
     * Notify replicator event(such as created, error, destroyed) to replicatorStateListener which is implemented by users for none status.
     *
     * @param replicator replicator object
     * @param event      replicator's state listener event type
     */
    private static void notifyReplicatorStatusListener(final Replicator replicator, final ReplicatorEvent event) {
        notifyReplicatorStatusListener(replicator, event, null);
    }

    /**
     * notify the follower to apply to state machine
     * @param index
     */
    public void notifyApply(long index) {
        RpcRequests.NotifyFollowerToApplyRequest.Builder builder
                = RpcRequests.NotifyFollowerToApplyRequest.newBuilder();
        builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
        builder.setLastIndex(index);
        NodeImpl.getNodeImple().getEnClosureRpcRequest()
                .handleToApplyRequest(builder.build(),rpcServices,true);
    }


    public void sendEmptyEntries(final boolean isHeartBeat) {
        LOG.debug("Start to send empty entries to {} isHeartbeat:{}",getEndpoint(),isHeartBeat);
        try {


            RpcRequests.AppendEntriesRequest.Builder builder = RpcRequests.AppendEntriesRequest.newBuilder();
            if (isHeartBeat) {
                builder.setCommittedIndex(NodeImpl.getNodeImple().getStableLogIndex().longValue());
                builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
                builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
                builder.setGroupId(NodeImpl.getNodeImple().getNodeId().getGroupId());

//                builder.setPrevLogIndex(NodeImpl.getNodeImple().getLastLogIndex().get());
//                builder.setPrevLogTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
            } else {
                // Sending a probe request.
                builder.setCommittedIndex(NodeImpl.getNodeImple().getStableLogIndex().longValue());
                builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
                builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
                builder.setGroupId(NodeImpl.getNodeImple().getNodeId().getGroupId());
                builder.setPort(NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getPort());
                builder.setAddress(NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getAddress());
                builder.setTaskPort(NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getTaskPort());
            }
            RpcRequests.AppendEntriesRequest request = builder.build();

            NodeImpl.getNodeImple().getEnClosureRpcRequest()
                    .handleAppendEntriesRequest(request,getRpcServices(),true);
            LOG.debug("Send emptyAppendEntries request to {} at index {} on term {}"
                    , getOptions().getPeerId().getPeerName()
                    , NodeImpl.getNodeImple().getStableLogIndex(),
                    NodeImpl.getNodeImple().getLastLogTerm());

            Runnable runnable = () -> sendEmptyEntries(true);
            //Runnable runnable = () -> System.out.println(123123123);

            ScheduledFuture scheduledFuture = getTimerManager().schedule(runnable,
                    getOptions().getDynamicHeartBeatTimeoutMs()/2, TimeUnit.MILLISECONDS);

            LOG.debug("Set future task for heartbeat delay time:{} isdone:{}",
                    getOptions().getDynamicHeartBeatTimeoutMs(),
                    scheduledFuture.isDone());
        } catch (Exception e) {
            LOG.error("Replicator error {}",e.getMessage());
        }
    }

    void sendReadIndexRequest(long index) {
        RpcRequests.AppendEntriesRequest.Builder builder = RpcRequests.AppendEntriesRequest.newBuilder();
        builder.setCommittedIndex(NodeImpl.getNodeImple().getStableLogIndex().longValue());
        builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
        builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
        builder.setGroupId(NodeImpl.getNodeImple().getNodeId().getGroupId());
//        builder.setPrevLogIndex(NodeImpl.getNodeImple().getLastLogIndex().get());
//        builder.setPrevLogTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
        builder.setReadIndex(index);
        NodeImpl.getNodeImple().getEnClosureRpcRequest()
                .handleReadHeartbeatrequest(builder.build(),getRpcServices(),true);
    }

    void handleProbeOrFollowerDisOrderResponse(long currentIndexOfFollower){

        //getWriteLock().lock();
        try {
            //setLastInflightIndex(currentIndexOfFollower);
            //Check if Inflight has the log
            Inflight inflightLast = getInflights().peekLast();
            //need to satisfy the currentIndexOfFollower smaller than inflight
            if (inflightLast != null &&
                    currentIndexOfFollower < (inflightLast.getStartIndex() + inflightLast.getSize())) {
                EventTranslator<LogEntryEvent> entryEventTranslator = (event, sequence) ->
                {
                    event.entry = NodeImpl.getNodeImple().getLogManager().getEntry(currentIndexOfFollower);

                };
                getRingBuffer().publishEvent(entryEventTranslator);
            }else{
                //if not satisfy then cover the follower with leader log FROM THE BEGINNING
                EventTranslator<LogEntryEvent> entryEventTranslator = (event, sequence) ->
                {
                    event.entry = NodeImpl.getNodeImple().getLogManager().getEntry(0);

                };
                getRingBuffer().publishEvent(entryEventTranslator);
            }

        } catch (Exception e) {
            LOG.error("handleProbeOrFollowerDisOrderResponse error {}",e.getMessage());
            e.printStackTrace();
        }finally {
            //getWriteLock().unlock();
        }
    }

    public void start() {


        LOG.info("Send emptyAppendEntries request to {} at index {} on term {}"
                , getOptions().getPeerId().getPeerName()
                , NodeImpl.getNodeImple().getStableLogIndex(), NodeImpl.getNodeImple().getLastLogTerm());

        sendEmptyEntries(false);

    }

    public void stop() {
        getTimerManager().shutdown();
    }


    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public Map<Long, Inflight> getInflightMap() {
        return inflightMap;
    }

    private Object getOpts() {
        return this.options;
    }



    public ReplicatorOptions getOptions() {
        return options;
    }

    public AtomicLong getCurrentSuccessTerm() {
        return currentSuccessTerm;
    }

    public void setCurrentSuccessTerm(AtomicLong currentSuccessTerm) {
        this.currentSuccessTerm = currentSuccessTerm;
    }

    public AtomicLong getCurrentSuccessIndex() {
        return currentSuccessIndex;
    }

    public void setCurrentSuccessIndex(AtomicLong currentSuccessIndex) {
        this.currentSuccessIndex = currentSuccessIndex;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public ReplicatorState getState() {
        return state;
    }

    public void setState(ReplicatorState state) {
        this.state = state;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    public RpcServices getRpcServices() {
        return rpcServices;
    }



    public ReentrantReadWriteLock getReentrantLock() {
        return reentrantLock;
    }

    public void setReentrantLock(ReentrantReadWriteLock reentrantLock) {
        this.reentrantLock = reentrantLock;
    }

    public ReentrantReadWriteLock.WriteLock getWriteLock() {
        return writeLock;
    }

    public void setWriteLock(ReentrantReadWriteLock.WriteLock writeLock) {
        this.writeLock = writeLock;
    }

    public ReentrantReadWriteLock.ReadLock getReadLock() {
        return readLock;
    }

    public void setReadLock(ReentrantReadWriteLock.ReadLock readLock) {
        this.readLock = readLock;
    }

    public long getLastInflightIndex() {
        return lastInflightIndex;
    }

    public void setLastInflightIndex(long lastInflightIndex) {
        this.lastInflightIndex = lastInflightIndex;
    }



    public void setRpcServices(RpcServices rpcServices) {
        this.rpcServices = rpcServices;
    }

    public List<RpcRequests.AppendEntriesRequest> getAppendEntriesRequestList() {
        return appendEntriesRequestList;
    }

    public ArrayDeque<Inflight> getInflights() {
        return inflights;
    }

    public void setAppendEntriesRequestList(List<RpcRequests.AppendEntriesRequest> appendEntriesRequestList) {
        this.appendEntriesRequestList = appendEntriesRequestList;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public Disruptor<LogEntryEvent> getDisruptor() {
        return disruptor;
    }

    public void setDisruptor(Disruptor<LogEntryEvent> disruptor) {
        this.disruptor = disruptor;
    }

    public RingBuffer<LogEntryEvent> getRingBuffer() {
        return ringBuffer;
    }

    public void setRingBuffer(RingBuffer<LogEntryEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public State getFollowerState() {
        return followerState;
    }

    public void setFollowerState(State followerState) {
        this.followerState = followerState;
    }
}

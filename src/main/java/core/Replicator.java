package core;

/**
 * Created by 周思成 on  2020/4/5 23:34
 * @author Mike
 */


import com.alipay.remoting.NamedThreadFactory;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import config.ReplicatorOptions;
import entity.*;
import exceptions.LogExceptionHandler;
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
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

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

    }


    private static final Logger LOG                    = LoggerFactory.getLogger(Replicator.class);
    private final ReplicatorOptions          options;
    private TimerManager timerManager;
    private RpcServices rpcServices;
    private ReplicatorState state;
    private State followerState;
    private AtomicLong currentSuccessTerm;
    private AtomicLong currentSuccessIndex;
    private List<RpcRequests.AppendEntriesRequest> appendEntriesRequestList;
    private Disruptor<LogEntry> disruptor;
    private RingBuffer<LogEntry> ringBuffer;
    // In-flight RPC requests, FIFO queue
    private final ArrayDeque<Inflight> inflights              = new ArrayDeque<>();

    public Replicator(ReplicatorOptions options,RpcServices rpcServices) {
        this.state = ReplicatorState.IDLE;
        this.options = options;
        this.rpcServices = rpcServices;

        this.followerState = State.Probe;
        this.disruptor = DisruptorBuilder.<LogEntry>newInstance()
                .setRingBufferSize(NodeOptions.getNodeOptions().getDisruptorBufferSize())
                .setEventFactory(new LogEntryEventFactory())
                .setThreadFactory(new NamedThreadFactory("JRaft-Replicator-Disruptor-", true))
                .setProducerType(ProducerType.MULTI)
                .setWaitStrategy(new BlockingWaitStrategy())
                .build();
        disruptor.handleEventsWith( new ReplicatorHandler());
        disruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
    }

    private  class ReplicatorHandler implements EventHandler<LogEntry> {


        @Override
        public void onEvent(LogEntry logEntry, long l, boolean b) throws Exception {
            if (b && !getAppendEntriesRequestList().isEmpty()){
               final RpcRequests.AppendEntriesRequests.Builder builder
                       = RpcRequests.AppendEntriesRequests.newBuilder();
                builder.addAllArgs(getAppendEntriesRequestList());
                RpcRequests.AppendEntriesRequests appendEntriesRequests = builder.build();
                rpcServices.handleApendEntriesRequests(appendEntriesRequests);

                Inflight inflight = new Inflight(
                        getAppendEntriesRequestList().get(0).getCommittedIndex(),
                        getAppendEntriesRequestList().size(),
                        appendEntriesRequests.getSerializedSize());
                getInflights().add(inflight);
                getAppendEntriesRequestList().clear();

                return;
            }
            if(!b){
                final RpcRequests.AppendEntriesRequest.Builder builder
                        = RpcRequests.AppendEntriesRequest.newBuilder();
                builder.setData(ByteString.copyFrom(logEntry.getData()));
                builder.setCommittedIndex(logEntry.getId().getIndex());
                builder.setTerm(logEntry.getId().getTerm());
                builder.setPeerId(logEntry.getLeaderId().getId());
                getAppendEntriesRequestList().add(builder.build());
            }
        }
    }
    private static class LogEntryEventFactory implements EventFactory<LogEntry> {

        @Override
        public LogEntry newInstance() {
            return new LogEntry();
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


    public void sendEmptyEntries(final boolean isHeartBeat) {
        RpcRequests.AppendEntriesRequest.Builder builder = RpcRequests.AppendEntriesRequest.newBuilder();
        if(isHeartBeat){
            builder.setCommittedIndex(NodeImpl.getNodeImple().getLastLogIndex().longValue());
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().incrementAndGet());
            builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            builder.setGroupId(NodeImpl.getNodeImple().getNodeId().getGroupId());
            builder.setPrevLogIndex(getCurrentSuccessIndex().get());
            builder.setPrevLogTerm(getCurrentSuccessTerm().get());
        }
        else {
            // Sending a probe request.
            builder.setCommittedIndex(NodeImpl.getNodeImple().getLastLogIndex().longValue());
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().incrementAndGet());
            builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            builder.setGroupId(NodeImpl.getNodeImple().getNodeId().getGroupId());

        }
        RpcRequests.AppendEntriesRequest request = builder.build();
        getRpcServices().handleApendEntriesRequest(request);

        LOG.info("Send emptyAppendEntries request to {} at CommittedIndex {} on PrevLogTerm {}, index {} term {}"
                , getOptions().getPeerId().getPeerName()
                , request.getCommittedIndex(), request.getPrevLogTerm()
                ,request.getCommittedIndex(),request.getTerm());


    }

    void sendAppendEntriesRequest(){

    }

    public void start() {


        LOG.info("Send emptyAppendEntries request to {} at {} on term {}"
                , getOptions().getPeerId().getPeerName()
                , getOptions().getNode().getLastLogTerm(), NodeImpl.getNodeImple().getLastLogTerm());

        sendEmptyEntries(false);

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

    public Disruptor<LogEntry> getDisruptor() {
        return disruptor;
    }

    public void setDisruptor(Disruptor<LogEntry> disruptor) {
        this.disruptor = disruptor;
    }

    public RingBuffer<LogEntry> getRingBuffer() {
        return ringBuffer;
    }

    public void setRingBuffer(RingBuffer<LogEntry> ringBuffer) {
        this.ringBuffer = ringBuffer;
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

    public State getFollowerState() {
        return followerState;
    }

    public void setFollowerState(State followerState) {
        this.followerState = followerState;
    }
}

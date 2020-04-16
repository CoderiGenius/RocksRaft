package core;

/**
 * Created by 周思成 on  2020/4/5 23:34
 * @author Mike
 */


import com.alipay.remoting.NamedThreadFactory;
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
        final Future<Message> rpcFuture;

        // Request sequence.
        final int             seq;
        public Inflight( final long startIndex, final int count, final int size,
                        final int seq, final Future<Message> rpcFuture) {
            super();
            this.seq = seq;
            this.count = count;
            this.startIndex = startIndex;
            this.size = size;
            this.rpcFuture = rpcFuture;
        }
        @Override
        public String toString() {
            return "Inflight [count=" + this.count + ", startIndex=" + this.startIndex + ", size=" + this.size
                    + ", rpcFuture=" + this.rpcFuture  + ", seq=" + this.seq + "]";
        }

    }


    private static final Logger LOG                    = LoggerFactory.getLogger(Replicator.class);
    private final ReplicatorOptions          options;
    private TimerManager timerManager;
    private RpcServices rpcServices;
    private ReplicatorState state;
    private AtomicLong currentSuccessTerm;
    private AtomicLong currentSuccessIndex;
    private List<Task> taskList;
    private Disruptor<Task> disruptor;
    private RingBuffer<Task> ringBuffer;
    public Replicator(ReplicatorOptions options,RpcServices rpcServices) {
        this.state = ReplicatorState.IDLE;
        this.options = options;
        this.rpcServices = rpcServices;
        this.state = State.Probe;
        this.disruptor = DisruptorBuilder.<Task>newInstance()
                .setRingBufferSize(NodeOptions.getNodeOptions().getDisruptorBufferSize())
                .setEventFactory(new TaskEventFactory())
                .setThreadFactory(new NamedThreadFactory("JRaft-Replicator-Disruptor-", true))
                .setProducerType(ProducerType.MULTI)
                .setWaitStrategy(new BlockingWaitStrategy())
                .build();
        disruptor.handleEventsWith( new ReplicatorHandler());
        disruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
    }

    private  class ReplicatorHandler implements EventHandler<entity.Task> {

        @Override
        public void onEvent(Task task, long l, boolean b) throws Exception {

        }
    }
    private static class TaskEventFactory implements EventFactory<Task> {

        @Override
        public Task newInstance() {
            return new Task();
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

    public Disruptor<Task> getDisruptor() {
        return disruptor;
    }

    public void setDisruptor(Disruptor<Task> disruptor) {
        this.disruptor = disruptor;
    }

    public RingBuffer<Task> getRingBuffer() {
        return ringBuffer;
    }

    public void setRingBuffer(RingBuffer<Task> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void setRpcServices(RpcServices rpcServices) {
        this.rpcServices = rpcServices;
    }
}

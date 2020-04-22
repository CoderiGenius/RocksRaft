package core;

import com.alipay.remoting.NamedThreadFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.sun.corba.se.spi.orbutil.fsm.FSM;
import entity.*;
import exceptions.LogExceptionHandler;
import exceptions.RaftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DisruptorBuilder;
import utils.Utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by 周思成 on  2020/4/22 11:11
 */

public class FSMCallerImpl implements FSMCaller {
    private static final Logger LOG = LoggerFactory.getLogger(FSMCallerImpl.class);


    private LogManager logManager;
    private StateMachine fsm;
    //private ClosureQueue                                            closureQueue;
    private final AtomicLong lastAppliedIndex = new AtomicLong(0);
    private long lastAppliedTerm;
    private Closure afterShutdown;
    private NodeImpl node;
    private volatile TaskType currTask;
    private final AtomicLong applyingIndex = new AtomicLong(0);
    private volatile RaftException error;
    private Disruptor<ApplyTask> disruptor;
    private RingBuffer<ApplyTask> taskQueue;
    private volatile CountDownLatch shutdownLatch;


    public FSMCallerImpl() {
    }

    @Override
    public boolean init(final FSMCallerOptions opts) {
        this.logManager = opts.getLogManager();
        this.fsm = opts.getFsm();
        this.lastAppliedIndex.set(opts.getBootstrapId().getIndex());
        this.afterShutdown = opts.getAfterShutdown();
        this.node = opts.getNode();
        this.lastAppliedTerm = opts.getBootstrapId().getTerm();
        this.lastAppliedIndex.set(opts.getBootstrapId().getIndex());
        this.disruptor = DisruptorBuilder.<ApplyTask> newInstance() //
                .setEventFactory(new ApplyTaskFactory()) //
                .setRingBufferSize(opts.getDisruptorBufferSize()) //
                .setThreadFactory(new NamedThreadFactory("JRaft-FSMCaller-Disruptor-", true)) //
                .setProducerType(ProducerType.MULTI) //
                .setWaitStrategy(new BlockingWaitStrategy()) //
                .build();
        this.disruptor.handleEventsWith(new ApplyTaskHandler());
        this.disruptor.setDefaultExceptionHandler(new LogExceptionHandler<Object>(getClass().getSimpleName()));
        this.taskQueue = this.disruptor.start();




        LOG.info("Starts FSMCaller successfully.");
        return true;
    }
    private class ApplyTaskHandler implements EventHandler<ApplyTask> {
        // max committed index in current batch, reset to -1 every batch
        private long maxCommittedIndex = -1;

        @Override
        public void onEvent(final ApplyTask event, final long sequence, final boolean endOfBatch) throws Exception {
            this.maxCommittedIndex = runApplyTask(event, this.maxCommittedIndex, endOfBatch);
        }
    }

    private static class ApplyTaskFactory implements EventFactory<ApplyTask> {

        @Override
        public ApplyTask newInstance() {
            return new ApplyTask();
        }
    }


    private boolean enqueueTask(final EventTranslator<ApplyTask> tpl) {
        if (this.shutdownLatch != null) {
            // Shutting down
            LOG.warn("FSMCaller is stopped, can not apply new task.");
            return false;
        }
        if (!this.taskQueue.tryPublishEvent(tpl)) {
            onError(new RaftException( new Status(RaftError.EBUSY,
                    "FSMCaller is overload.")));
            return false;
        }
        return true;
    }


    private long runApplyTask(ApplyTask task, long maxCommittedIndex, boolean endOfBatch) {

        if (task.type == TaskType.COMMITTED) {
            if (task.committedIndex > maxCommittedIndex) {
                maxCommittedIndex = task.committedIndex;
            }
        }else {
            if (maxCommittedIndex >= 0) {
                this.currTask = TaskType.COMMITTED;
                doCommitted(maxCommittedIndex);
                maxCommittedIndex = -1L; // reset maxCommittedIndex
            }
        }


        if (endOfBatch && maxCommittedIndex >= 0) {
            this.currTask = TaskType.COMMITTED;
            doCommitted(maxCommittedIndex);
            maxCommittedIndex = -1L; // reset maxCommittedIndex
        }
        this.currTask = TaskType.IDLE;
        return maxCommittedIndex;
    }

    private void doCommitted(long maxCommittedIndex) {
        final long lastAppliedIndex = this.lastAppliedIndex.get();
        // We can tolerate the disorder of committed_index
        if (lastAppliedIndex >= maxCommittedIndex) {
            return;
        }
        final IteratorImpl iterImpl = new IteratorImpl(this.fsm, this.logManager,
                lastAppliedIndex, maxCommittedIndex, this.applyingIndex);
        while (iterImpl.isGood()) {
            // Apply data task to user state machine
            doApplyTasks(iterImpl);
        }
    }

    private void doApplyTasks(IteratorImpl iterImpl) {
        final IteratorWrapper iter = new IteratorWrapper(iterImpl);


            this.fsm.onApply(iter);

        if (iter.hasNext()) {
            LOG.error("Iterator is still valid, did you return before iterator reached the end?");
        }
        // Try move to next in case that we pass the same log twice.
        iter.next();
    }

    @Override
    public boolean onCommitted(long committedIndex) {
        return enqueueTask((task, sequence) -> {
            task.type = TaskType.COMMITTED;
            task.committedIndex = committedIndex;
        });
    }

    @Override
    public boolean onLeaderStart(long term) {
        return false;
    }

    @Override
    public boolean onStartFollowing(LeaderChangeContext ctx) {
        return false;
    }

    @Override
    public boolean onStopFollowing(LeaderChangeContext ctx) {
        return false;
    }

    @Override
    public boolean onError(RaftException error) {
        return false;
    }

    @Override
    public long getLastAppliedIndex() {
        return 0;
    }

    private enum TaskType {
        IDLE,
        COMMITTED,
        SNAPSHOT_SAVE,
        SNAPSHOT_LOAD,
        LEADER_STOP,
        LEADER_START,
        START_FOLLOWING,
        STOP_FOLLOWING,
        SHUTDOWN,
        FLUSH,
        ERROR;
    }

    private static class ApplyTask {
        TaskType type;
        // union fields
        long committedIndex;
        long term;
        Status status;
        LeaderChangeContext leaderChangeCtx;
        Closure done;
        CountDownLatch shutdownLatch;

        public void reset() {
            this.type = null;
            this.committedIndex = 0;
            this.term = 0;
            this.status = null;
            this.leaderChangeCtx = null;
            this.done = null;
            this.shutdownLatch = null;
        }
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public StateMachine getFsm() {
        return fsm;
    }

    public void setFsm(StateMachine fsm) {
        this.fsm = fsm;
    }

    public long getLastAppliedTerm() {
        return lastAppliedTerm;
    }

    public void setLastAppliedTerm(long lastAppliedTerm) {
        this.lastAppliedTerm = lastAppliedTerm;
    }

    public Closure getAfterShutdown() {
        return afterShutdown;
    }

    public void setAfterShutdown(Closure afterShutdown) {
        this.afterShutdown = afterShutdown;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }

    public TaskType getCurrTask() {
        return currTask;
    }

    public void setCurrTask(TaskType currTask) {
        this.currTask = currTask;
    }

    public AtomicLong getApplyingIndex() {
        return applyingIndex;
    }

    public RaftException getError() {
        return error;
    }

    public void setError(RaftException error) {
        this.error = error;
    }

    public Disruptor<ApplyTask> getDisruptor() {
        return disruptor;
    }

    public void setDisruptor(Disruptor<ApplyTask> disruptor) {
        this.disruptor = disruptor;
    }

    public RingBuffer<ApplyTask> getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(RingBuffer<ApplyTask> taskQueue) {
        this.taskQueue = taskQueue;
    }

    public CountDownLatch getShutdownLatch() {
        return shutdownLatch;
    }

    public void setShutdownLatch(CountDownLatch shutdownLatch) {
        this.shutdownLatch = shutdownLatch;
    }
}

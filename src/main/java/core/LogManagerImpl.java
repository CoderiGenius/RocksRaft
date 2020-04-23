package core;

import com.alipay.remoting.NamedThreadFactory;
import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import config.LogManagerOptions;
import config.LogStorageOptions;
import entity.*;
import exceptions.LogExceptionHandler;
import exceptions.LogStorageException;
import exceptions.RaftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.EnumOutter;
import storage.LogStorage;
import utils.DisruptorBuilder;
import utils.Requires;
import utils.SegmentList;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/4/7 19:59
 */

public class LogManagerImpl implements LogManager {

    private LogId                                            diskId                 = new LogId(0, 0);
    private static final int APPEND_LOG_RETRY_TIMES = 50;
    private static final Logger LOG = LoggerFactory
            .getLogger(LogManagerImpl.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock writeLock = this.lock.writeLock();
    private final Lock readLock = this.lock.readLock();
    private LogStorage logStorage;
    private final SegmentList<LogEntry> logsInMemory = new SegmentList<>(true);
    private Disruptor<StableClosureEvent> disruptor;
    private RingBuffer<StableClosureEvent> diskQueue;
    private LogId appliedId = new LogId(0, 0);
    private Options options;
    private long firstLogIndex;
    private long lastLogIndex;
    private boolean stopped;
    private boolean hasError = false;
    private FSMCaller fsmCaller;

    /**
     * Closure to to run in stable state.
     *
     * @author boyan (boyan@alibaba-inc.com)
     * <p>
     * 2018-Apr-04 4:35:29 PM
     */

    @Override
    public void join() throws InterruptedException {

    }

    private static class StableClosureEvent {
        StableClosure done;
        EventType type;

        void reset() {
            this.done = null;
            this.type = null;
        }
    }

    private enum EventType {
        // other event type.
        OTHER,
        // reset
        RESET,
        // truncate log from prefix
        TRUNCATE_PREFIX,
        // truncate log from suffix
        TRUNCATE_SUFFIX,
        SHUTDOWN,
        // get last log id
        LAST_LOG_ID
    }

    @Override
    public void appendEntries(final List<LogEntry> entries, final StableClosure done) {
        //Requires.requireNonNull(done, "done");
        this.writeLock.lock();
        try {
            for (int i = 0; i < entries.size(); i++) {
                //final LogEntry logEntry = entries.get(i);
                if (!entries.isEmpty()) {
                    done.setFirstLogIndex(entries.get(0).getId().getIndex());
                    this.logsInMemory.addAll(entries);
                }
                done.setEntries(entries);
            }
            int retryTimes = 0;
            final EventTranslator<StableClosureEvent> translator = (event, sequence) -> {
                event.reset();
                event.type = EventType.OTHER;
                event.done = done;
            };
            while (true) {
                if (tryOfferEvent(done, translator)) {
                    break;
                } else {
                    retryTimes++;
                    if (retryTimes > APPEND_LOG_RETRY_TIMES) {
                        reportError(RaftError.EBUSY.getNumber(), "LogManager is busy, disk queue overload.");
                        return;
                    }
                    Thread.yield();
                }
            }
        } catch (Exception e) {
            LOG.error("AppendEntries error {}", e.getMessage());
        } finally {
            this.writeLock.unlock();
        }

    }


    private boolean tryOfferEvent(final StableClosure done, final EventTranslator<StableClosureEvent> translator) {
        if (this.stopped) {
            Utils.runClosureInThread(done, new Status(RaftError.ESTOP, "Log manager is stopped."));
            return true;
        }
        return this.diskQueue.tryPublishEvent(translator);
    }

    private void reportError(final int code, final String fmt, final Object... args) {
        this.hasError = true;
        final RaftException error = new RaftException(EnumOutter.ErrorType.ERROR_TYPE_LOG);
        error.setStatus(new Status(code, fmt, args));
        this.fsmCaller.onError(error);
    }

    @Override
    public LogEntry getEntry(long index) {

        this.readLock.lock();
        try {
            if (index > this.lastLogIndex || index < this.firstLogIndex) {
                return null;
            }
            final LogEntry entry = getEntryFromMemory(index);
            if (entry != null) {
                return entry;
            }
        } finally {
            this.readLock.unlock();
        }
        final LogEntry entry = this.logStorage.getEntry(index);
        if (entry == null) {
            reportError(RaftError.EIO.getNumber(), "Corrupted entry at index=%d, not found", index);
        }
        // Validate checksum
//        if (entry != null && this.raftOptions.isEnableLogEntryChecksum() && entry.isCorrupted()) {
//            String msg = String.format("Corrupted entry at index=%d, term=%d, expectedChecksum=%d, realChecksum=%d",
//                    index, entry.getId().getTerm(), entry.getChecksum(), entry.checksum());
//            // Report error to node and throw exception.
//            reportError(RaftError.EIO.getNumber(), msg);
//            throw new LogEntryCorruptedException(msg);
//        }
        return entry;
    }
    protected LogEntry getEntryFromMemory(final long index) {
        LogEntry entry = null;
        if (!this.logsInMemory.isEmpty()) {
            final long firstIndex = this.logsInMemory.peekFirst().getId().getIndex();
            final long lastIndex = this.logsInMemory.peekLast().getId().getIndex();
            if (lastIndex - firstIndex + 1 != this.logsInMemory.size()) {
                throw new IllegalStateException(String.format("lastIndex=%d,firstIndex=%d,logsInMemory=[%s]",
                        lastIndex, firstIndex, descLogsInMemory()));
            }
            if (index >= firstIndex && index <= lastIndex) {
                entry = this.logsInMemory.get((int) (index - firstIndex));
            }
        }
        return entry;
    }

    private String descLogsInMemory() {
        final StringBuilder sb = new StringBuilder();
        boolean wasFirst = true;
        for (int i = 0; i < this.logsInMemory.size(); i++) {
            LogEntry logEntry = this.logsInMemory.get(i);
            if (!wasFirst) {
                sb.append(",");
            } else {
                wasFirst = false;
            }
            sb.append("<id:(").append(logEntry.getId().getTerm()).append(",").append(logEntry.getId().getIndex())
                    .append("),type:").append(logEntry.getType()).append(">");
        }
        return sb.toString();
    }
    @Override
    public long getTerm(long index) {
        return 0;
    }

    @Override
    public long getLastLogIndex() {
        return 0;
    }

    @Override
    public long getLastLogIndex(boolean isFlush) {
        return 0;
    }

    @Override
    public LogId getLastLogId(boolean isFlush) {
        return null;
    }

    @Override
    public boolean init(final LogManagerOptions opts) throws LogStorageException {
        this.writeLock.lock();
        try {
            if (opts.getLogStorage() == null) {
                LOG.error("Fail to init log manager, log storage is null");
                return false;
            }
            this.options = opts.getOptions();
            this.logStorage = opts.getLogStorage();
            if (!logStorage.init()) {

                throw new LogStorageException();
            }

            this.firstLogIndex = this.logStorage.getFirstLogIndex();
            this.lastLogIndex = this.logStorage.getLastLogIndex();
            this.disruptor = DisruptorBuilder.<StableClosureEvent> newInstance()
                    .setEventFactory(new StableClosureEventFactory())
                    .setRingBufferSize(opts.getDisruptorBufferSize())
                    .setThreadFactory(new NamedThreadFactory("JRaft-LogManager-Disruptor-", true))
                    .setProducerType(ProducerType.MULTI)
                    /*
                     *  Use timeout strategy in log manager. If timeout happens, it will called reportError to halt the node.
                     */
                    .setWaitStrategy(new TimeoutBlockingWaitStrategy(
                            NodeOptions.getNodeOptions()
                                    .getDisruptorPublishEventWaitTimeoutSecs(), TimeUnit.SECONDS))
                    .build();
            this.disruptor.handleEventsWith(new StableClosureEventHandler());
            this.disruptor.setDefaultExceptionHandler(
                    new LogExceptionHandler<Object>(this.getClass().getSimpleName(),
                            (event, ex) -> reportError(-1, "LogManager handle event error")));
            this.diskQueue = this.disruptor.start();
            this.stopped = false;

        } catch (Exception e) {
            LOG.error("init LogManager error {}", e.getMessage());
        } finally {
            this.writeLock.unlock();
        }
        return true;

    }

    private static class StableClosureEventFactory implements EventFactory<StableClosureEvent> {

        @Override
        public StableClosureEvent newInstance() {
            return new StableClosureEvent();
        }
    }

    private class StableClosureEventHandler implements EventHandler<StableClosureEvent> {

        LogId               lastId  = LogManagerImpl.this.diskId;
        List<StableClosure> storage = new ArrayList<>(256);
        AppendBatcher       ab      = new AppendBatcher(this.storage, 256, new ArrayList<>(),
                LogManagerImpl.this.diskId);

        @Override
        public void onEvent(StableClosureEvent stableClosureEvent, long sequence, boolean endOfBatch) throws Exception {
            final StableClosure done = stableClosureEvent.done;
            if (done.getEntries() != null && !done.getEntries().isEmpty()) {
                this.ab.append(done);
            }else {
                this.lastId = this.ab.flush();
            }
            if (endOfBatch) {
                this.lastId = this.ab.flush();
                setDiskId(this.lastId);
                //send to all replicators
                NodeImpl.getNodeImple().getReplicatorGroup()
                        .sendAppendEntriesToAllReplicator(stableClosureEvent.done.getEntries());
            }
        }
    }

    private void setDiskId(final LogId id) {
        if (id == null) {
            return;
        }
        LogId clearId;
        this.writeLock.lock();
        try {
            if (id.compareTo(this.diskId) < 0) {
                return;
            }
            this.diskId = id;
            clearId = this.diskId.compareTo(this.appliedId) <= 0 ? this.diskId : this.appliedId;
        } finally {
            this.writeLock.unlock();
        }
        if (clearId != null) {
            clearMemoryLogs(clearId);
        }
    }
    private void clearMemoryLogs(final LogId id) {
        this.writeLock.lock();
        try {

            this.logsInMemory.removeFromFirstWhen(entry -> entry.getId().compareTo(id) <= 0);
        } finally {
            this.writeLock.unlock();
        }
    }
    private class AppendBatcher {
        List<StableClosure> storage;
        int                 cap;
        int                 size;
        int                 bufferSize;
        List<LogEntry>      toAppend;
        LogId               lastId;

        public AppendBatcher(final List<StableClosure> storage, final int cap, final List<LogEntry> toAppend,
                             final LogId lastId) {
            super();
            this.storage = storage;
            this.cap = cap;
            this.toAppend = toAppend;
            this.lastId = lastId;
        }

        LogId flush() {
            if (this.size > 0) {
                this.lastId = appendToStorage(this.toAppend);
                for (int i = 0; i < this.size; i++) {
                    this.storage.get(i).getEntries().clear();
                    Status st = null;
                    try {
                        if (LogManagerImpl.this.hasError) {
                            st = new Status(RaftError.EIO, "Corrupted LogStorage");
                        } else {
                            st = Status.OK();
                            st.setFirstIndex(storage.get(i).getFirstLogIndex());
                            st.setLastIndex(storage.get(i).getFirstLogIndex()+size);
                            //st.setTerm(storage.get(i));
                        }
                        this.storage.get(i).run(st);
                    } catch (Throwable t) {
                        LOG.error("Fail to run closure with status: {}.", st, t);
                    }
                }
                this.toAppend.clear();
                this.storage.clear();
            }
            this.size = 0;
            this.bufferSize = 0;
            return this.lastId;
        }

        void append(final StableClosure done) {
            if (this.size == this.cap || this.bufferSize >= NodeOptions.getNodeOptions().getMaxAppendBufferSize()) {
                flush();
            }
            this.storage.add(done);
            this.size++;
            this.toAppend.addAll(done.getEntries());
            for (final LogEntry entry : done.getEntries()) {
                this.bufferSize += entry.getData() != null ? entry.getData().remaining() : 0;
            }
        }
    }
    private LogId appendToStorage(final List<LogEntry> toAppend) {
        LogId lastId = null;
        if (!this.hasError) {
            final long startMs = Utils.monotonicMs();
            final int entriesCount = toAppend.size();
            try {
                int writtenSize = 0;
                for (final LogEntry entry : toAppend) {
                    writtenSize += entry.getData() != null ? entry.getData().remaining() : 0;
                }
                final int nAppent = this.logStorage.appendEntries(toAppend);
                if (nAppent != entriesCount) {
                    LOG.error("**Critical error**, fail to appendEntries, nAppent={}, toAppend={}", nAppent,
                            toAppend.size());
                    reportError(RaftError.EIO.getNumber(), "Fail to append log entries");
                }
                if (nAppent > 0) {
                    lastId = toAppend.get(nAppent - 1).getId();
                }
                toAppend.clear();
            } catch (Exception e) {
                LOG.error("appendToStorage error {}",e.getMessage());
            }
        }
        return lastId;
    }
    public ReadWriteLock getLock() {
        return lock;
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public Lock getReadLock() {
        return readLock;
    }

    public LogStorage getLogStorage() {
        return logStorage;
    }

    public void setLogStorage(LogStorage logStorage) {
        this.logStorage = logStorage;
    }

    public LogId getAppliedId() {
        return appliedId;
    }

    public void setAppliedId(LogId appliedId) {
        this.appliedId = appliedId;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public long getFirstLogIndex() {
        return firstLogIndex;
    }

    public void setFirstLogIndex(long firstLogIndex) {
        this.firstLogIndex = firstLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }
}

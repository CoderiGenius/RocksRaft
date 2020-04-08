package core;

import config.LogManagerOptions;
import config.LogStorageOptions;
import entity.LogEntry;
import entity.LogId;
import entity.LogManager;
import entity.Options;
import exceptions.LogStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.LogStorage;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/4/7 19:59
 */

public class LogManagerImpl implements LogManager {


    private static final Logger LOG                    = LoggerFactory
            .getLogger(LogManagerImpl.class);
    private final ReadWriteLock lock                   = new ReentrantReadWriteLock();
    private final Lock writeLock              = this.lock.writeLock();
    private final Lock                                       readLock               = this.lock.readLock();
    private LogStorage logStorage;
    private LogId appliedId = new LogId(0,0);
    private Options options;
    private long firstLogIndex;
    private long lastLogIndex;

    @Override
    public void join() throws InterruptedException {

    }

    @Override
    public void appendEntries(List<LogEntry> entries) {

    }

    @Override
    public LogEntry getEntry(long index) {
        return null;
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

            LogStorageOptions logStorageOptions = new LogStorageOptions();
            logStorageOptions.setLogStorageName(opts.getOptions().getCurrentNodeOptions().getLogStorageName());
            logStorageOptions.setLogStoragePath(opts.getOptions().getCurrentNodeOptions().getLogStoragePath());
            LogStorage logStorage = new LogStorageImpl(logStorageOptions);
            if ( ! logStorage.init()) {

                throw new LogStorageException();
            }

            this.firstLogIndex = this.logStorage.getFirstLogIndex();
            this.lastLogIndex = this.logStorage.getLastLogIndex();


        } catch (Exception e) {
            LOG.error("init LogManager error {}",e.getMessage());
        }finally {
            this.writeLock.unlock();
        }
        return true;

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

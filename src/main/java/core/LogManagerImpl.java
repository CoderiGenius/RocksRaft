package core;

import entity.LogEntry;
import entity.LogId;
import entity.LogManager;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/4/7 19:59
 */

public class LogManagerImpl implements LogManager {


    private final ReadWriteLock lock                   = new ReentrantReadWriteLock();
    private final Lock writeLock              = this.lock.writeLock();
    private final Lock                                       readLock               = this.lock.readLock();

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
}

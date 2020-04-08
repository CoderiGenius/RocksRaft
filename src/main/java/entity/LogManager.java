package entity;

import config.LogManagerOptions;
import exceptions.LogStorageException;

import java.util.List;

/**
 * Created by 周思成 on  2020/4/5 16:43
 */

public interface LogManager {

    /**
     * Wait the log manager to be shut down.
     *
     * @throws InterruptedException if the current thread is interrupted
     *         while waiting
     */
    void join() throws InterruptedException;

    /**
     * Append log entry vector and wait until it's stable (NOT COMMITTED!)
     *
     * @param entries log entries
     */
    void appendEntries(final List<LogEntry> entries);

    /**
     * Get the log entry at index.
     *
     * @param index the index of log entry
     * @return the log entry with {@code index}
     */
    LogEntry getEntry(final long index);


    /**
     * Get the log term at index.
     *
     * @param index the index of log entry
     * @return the term of log entry
     */
    long getTerm(final long index);

    /**
     * Get the last log index of log
     */
    long getLastLogIndex();

    /**
     * Get the last log index of log
     *
     * @param isFlush whether to flush from disk.
     */
    long getLastLogIndex(final boolean isFlush);

    /**
     * Return the id the last log.
     *
     * @param isFlush whether to flush all pending task.
     */
    LogId getLastLogId(final boolean isFlush);


    boolean init(final LogManagerOptions options) throws LogStorageException;
}

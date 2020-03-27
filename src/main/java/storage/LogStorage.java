package storage;

/**
 * Created by 周思成 on  2020/3/13 15:52
 */

import core.Lifecycle;
import entity.LogEntry;

import java.util.List;

/**
 * Log entry storage service.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Mar-12 3:43:54 PM
 */
public interface LogStorage  {

    /**
     * Returns first log index in log.
     */
    long getFirstLogIndex();

    /**
     * Returns last log index in log.
     */
    long getLastLogIndex();

    /**
     * Get logEntry by index.
     */
    LogEntry getEntry(final long index);

    /**
     * Get logEntry's term by index. This method is deprecated, you should use {@link #getEntry(long)} to get the log id's term.
     * @deprecated
     */
    @Deprecated
    long getTerm(final long index);

    /**
     * Append entries to log.
     */
    boolean appendEntry(final LogEntry entry);

    /**
     * Append entries to log, return append success number.
     */
    int appendEntries(final List<LogEntry> entries);

    /**
     * Delete logs from storage's head, [first_log_index, first_index_kept) will
     * be discarded.
     */
    boolean truncatePrefix(final long firstIndexKept);

    /**
     * Delete uncommitted logs from storage's tail, (last_index_kept, last_log_index]
     * will be discarded.
     */
    boolean truncateSuffix(final long lastIndexKept);

    /**
     * Drop all the existing logs and reset next log index to |next_log_index|.
     * This function is called after installing snapshot from leader.
     */
    boolean reset(final long nextLogIndex);
}
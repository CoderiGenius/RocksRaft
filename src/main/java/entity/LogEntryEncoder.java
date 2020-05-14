package entity;

import exceptions.RaftException;

/**
 * Created by 周思成 on  2020/3/13 16:54
 */

public interface LogEntryEncoder {

    /**
     * Encode a log entry into a byte array.
     * @param log log entry
     * @return encoded byte array
     */
    byte[] encode(LogEntry log) throws RaftException;
}
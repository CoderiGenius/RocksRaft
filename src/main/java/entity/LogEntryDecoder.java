package entity;

/**
 * Created by 周思成 on  2020/3/13 16:55
 */

public interface LogEntryDecoder {
    /**
     * Decode a log entry from byte array,
     * return null when fail to decode.
     * @param bs
     * @return
     */
    LogEntry decode(byte[] bs);
}

package entity;

/**
 * Created by 周思成 on  2020/3/13 16:54
 */

public interface LogEntryCodecFactory {
    /**
     * Returns a log entry encoder.
     * @return encoder instance
     */
    LogEntryEncoder encoder();

    /**
     * Returns a log entry decoder.
     * @return encoder instance
     */
    LogEntryDecoder decoder();
}
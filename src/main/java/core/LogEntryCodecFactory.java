package core;

import entity.LogEntryDecoder;
import entity.LogEntryEncoder;

/**
 * Created by 周思成 on  2020/4/7 20:54
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
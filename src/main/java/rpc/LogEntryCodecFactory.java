package rpc;

import entity.LogEntryDecoder;

/**
 * Created by 周思成 on  2020/5/7 12:25
 */

public interface LogEntryCodecFactory {


    public LogEntryEncoder encoder();


    public LogEntryDecoder decoder();
}

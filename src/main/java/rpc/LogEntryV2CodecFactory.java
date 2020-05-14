package rpc;

import entity.LogEntryDecoder;

/**
 * Created by 周思成 on  2020/5/7 12:31
 */

/**
 * V2(Now) log entry codec implementation, header format:
 *
 *   0      4
 *  +-+-+-+-+
 *  |Version|
 *  +-+-+-+-+
 *
 * @author Mike ZHOU
 *
 *
 */

public class LogEntryV2CodecFactory implements LogEntryCodecFactory {

    private static final LogEntryV2CodecFactory INSTANCE = new LogEntryV2CodecFactory();

    public static LogEntryV2CodecFactory getInstance() {
        return INSTANCE;
    }

    public static final int    HEADER_SIZE = 4;
    public static final byte[] HEADER_VERSION = int2Bytes(1);
    @Override
    public LogEntryEncoder encoder() {
        return LogEntryEncoder.INSTANCE;
    }

    @Override
    public LogEntryDecoder decoder() {
        return null;
    }

    public static byte[] int2Bytes(int num) {
        byte[] bytes = new byte[4];
        //通过移位运算，截取低8位的方式，将int保存到byte数组
        bytes[0] = (byte)(num >>> 24);
        bytes[1] = (byte)(num >>> 16);
        bytes[2] = (byte)(num >>> 8);
        bytes[3] = (byte)num;
        return bytes;
    }
}

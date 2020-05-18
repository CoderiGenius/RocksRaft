package rpc;

import com.google.protobuf.CodedOutputStream;
import entity.LogEntry;
import entity.Status;
import exceptions.RaftException;
import utils.Requires;

import java.nio.ByteBuffer;

/**
 * Created by 周思成 on  2020/5/7 11:54
 */


public class LogEntryEncoderV2 implements entity.LogEntryEncoder {

    public static final LogEntryEncoderV2 INSTANCE = new LogEntryEncoderV2();


    @Override
    public byte[] encode(LogEntry log) throws RaftException {
        Requires.requireNonNull(log,"The log entry is null");


        return log.getDataString().getBytes();
    }

//    @Override
//    public byte[] encode(LogEntry log) throws RaftException {
//        Requires.requireNonNull(log,"The log entry is null");
//
//        final LogId logId = log.getId();
//        final LogOuter.LogEntry.Builder builder = LogOuter.LogEntry.newBuilder();
//        builder.setIndex(logId.getIndex());
//        builder.setTerm(logId.getTerm());
//        builder.setData( ByteString.copyFrom(getByteArrayFromByteBuffer(log.getData())));
//        final LogOuter.LogEntry logEntry = builder.build();
//        final int bodyLen = logEntry.getSerializedSize();
//        final byte[] ret = new byte[LogEntryV2CodecFactory.HEADER_SIZE + bodyLen];
//        int i = 0;
//        for (; i <LogEntryV2CodecFactory.HEADER_VERSION.length ; i++) {
//            ret[i] = LogEntryV2CodecFactory.HEADER_VERSION[i];
//        }
//        // write body
//        writeToByteArray(logEntry, ret, i, bodyLen);
//        return ret;
//    }
//

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

    private void writeToByteArray(final LogOuter.LogEntry logEntry
            , final byte[] array, final int offset, final int len) throws RaftException {
        final CodedOutputStream output = CodedOutputStream.newInstance(array, offset, len);
        try {
            logEntry.writeTo(output);
            output.checkNoSpaceLeft();
        } catch (final Exception e) {
            Status status = new Status();
            status.setError(-1
                    ,"Serializing PBLogEntry to a byte array threw an IOException (should never happen).",e);
            throw new RaftException(status);
        }
    }
}

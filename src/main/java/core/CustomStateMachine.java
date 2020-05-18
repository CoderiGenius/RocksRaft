package core;

import entity.Iterator;
import entity.ReadTask;
import entity.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 周思成 on  2020/5/7 20:07
 */

public class CustomStateMachine extends StateMachineAdapter{
    private static final Logger LOG = LoggerFactory.getLogger(CustomStateMachine.class);
    private final Lock lock                  = new ReentrantLock();
    private final RocksDBStorage rocksDBStorage = RocksDBStorageImpl.getRocksDBStorage();

    @Override
    public void onApply(Iterator iter) {
        LOG.debug("onApply:{} operation:{}",iter.getIndex(),iter.getOperation());
        this.lock.lock();
        try {
            switch (iter.getOperation()){
                case "LOG":
                    while (iter.hasNext()){
                        rocksDBStorage.put(toBytes(iter.getIndex()),getByteArrayFromByteBuffer(iter.getData()));
                        NodeImpl.getNodeImple().handleLogApplied(iter.getIndex());
                        LOG.debug("Get from rocksdb test:key:{} value:{}",iter.getIndex(),new String(
                                rocksDBStorage.get(toBytes(iter.getIndex()))));
                        iter.next();
                    }

                    return;
                case "READ":
                    List<ReadTask> list = new ArrayList<>(iter.getSize());
                    while (iter.hasNext()){
                        ReadTask readTask = new ReadTask(
                                rocksDBStorage.get(getByteArrayFromByteBuffer(iter.getData())));
                        list.add(readTask);
                        iter.next();
                    }
                    NodeImpl.getNodeImple().getEnClosureClientRpcRequest()
                            .handleNotifyClient(list,true,new RpcResult());
                    return;
                default:
                    LOG.error("unknow apply operation");
            }

        } catch (Exception e) {
            LOG.error("CustomStateMachine apply error {}",e.getMessage());
        }finally {
            this.lock.unlock();
        }
    }

    public static byte[] toBytes(long val) {
        //System.out.println( "原来的长整形数据："+val );
        byte [] b = new byte[8];
        for (int i = 7; i > 0; i--) {
            //强制转型，后留下长整形的低8位
            b[i] = (byte) val;
            String str = Long.toBinaryString( val) ;
            String lb = Long.toBinaryString( b[i] ) ;
            String lb2 = Long.toBinaryString( b[i]&0xff ) ;


            //System.out.println("转换为字节："+ str );
            //System.out.println( lb );
            //System.out.println( lb2 );
            //向右移动8位，则第二次循环则计算第二个8位数
            val >>>= 8;
        }
        b[0] = (byte) val;
        return b;
    }
    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }
}

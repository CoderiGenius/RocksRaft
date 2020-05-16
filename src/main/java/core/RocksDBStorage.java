package core;

import entity.RocksBatch;
import org.rocksdb.RocksDBException;
import org.rocksdb.WriteBatch;
import org.rocksdb.WriteOptions;

import java.util.ArrayList;

/**
 * Created by 周思成 on  2020/4/17 12:19
 */

public interface RocksDBStorage {

   boolean put(byte[] var1,byte[] var2);

    boolean putBatch(ArrayList<RocksBatch> var);

    boolean init();

    void write(WriteOptions var1, WriteBatch var2) throws RocksDBException;

    byte[] get(byte[] var) throws RocksDBException;
}

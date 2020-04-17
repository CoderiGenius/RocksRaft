package core;

import entity.RocksBatch;

import java.util.ArrayList;

/**
 * Created by 周思成 on  2020/4/17 12:19
 */

public interface RocksDBStorage {

   boolean put(byte[] var1,byte[] var2);

    boolean putBatch(ArrayList<RocksBatch> var);

    boolean init();
}

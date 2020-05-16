package core;

import entity.RocksBatch;
import org.apache.commons.io.FilenameUtils;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/4/17 12:26
 */

public class RocksDBStorageImpl implements RocksDBStorage {
    public static final Logger LOG = LoggerFactory.getLogger(RocksDBStorageImpl.class);
    static{
        RocksDB.loadLibrary();

    }
    private static RocksDBStorageImpl rocksDBStorage=null;
    private boolean started = false;

    private RocksDBStorageImpl() {
        init();
    }

    private static RocksDB rocksDB;
    TransactionDB transactionDB;
    private final static String LOG_PATH = FilenameUtils.concat(NodeImpl.getNodeImple()
            .getOptions().getCurrentNodeOptions().getLogStoragePath()
            ,NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getLogStorageName());
    //private final static String LOG_PATH = "E://Log";
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Lock writeLock = this.readWriteLock.writeLock();
    protected final Lock readLock = this.readWriteLock.readLock();

    @Override
    public boolean put(byte[] var1, byte[] var2) {
        try {
            rocksDB.put(var1,var2);
            return true;
        } catch (RocksDBException e) {
            LOG.error("***Fatal error*** rocksDB PUT error {}",new String(var1));
        }
        return false;
    }

    @Override
    public boolean putBatch(ArrayList<RocksBatch> var) {
        getWriteLock().lock();
        try {
            for (RocksBatch rocksBatch :
                    var) {
                rocksDB.put(rocksBatch.getKey(),rocksBatch.getValue());
            }
            return true;
        } catch (RocksDBException e) {
            LOG.error("***Fatal error*** rocksDB PUT batch error {}",var);
        }
        return false;
    }

    @Override
    public boolean init() {
        getWriteLock().lock();
        if(started){
            return true;
        }

        LOG.info("Start to init RocksDBStorageImpl on logPath {}",LOG_PATH);

        try {
        Options options = new Options();
        options.setCreateIfMissing(true);
            //check if dir exist
            isChartPathExist(NodeImpl.getNodeImple()
                    .getOptions().getCurrentNodeOptions().getLogStoragePath());

            rocksDB = RocksDB.open(options, LOG_PATH);
            started = true;
            return true;
        } catch (RocksDBException e) {
            LOG.error("***Fatal error*** rocksDB open error {}",e.getMessage());
            return false;
        }finally {
            getWriteLock().unlock();
        }
    }

    @Override
    public void write(WriteOptions var1, WriteBatch var2) throws RocksDBException {
        rocksDB.write(var1,var2);
    }

    @Override
    public byte[] get(byte[] var) throws RocksDBException {
       return rocksDB.get(var);
    }


    public static RocksDB getRocksDB() {
        return rocksDB;
    }

    public static void setRocksDB(RocksDB rocksDB) {
        RocksDBStorageImpl.rocksDB = rocksDB;
    }

    public static String getLogPath() {
        return LOG_PATH;
    }



    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public Lock getReadLock() {
        return readLock;
    }

    public static RocksDBStorageImpl getRocksDBStorage() {
        if(rocksDBStorage==null){
            rocksDBStorage =  new RocksDBStorageImpl();
        }
        return rocksDBStorage;
    }
    private static void isChartPathExist(String dirPath) {

        File file = new File(dirPath);
        if (!file.exists()) {
        file.mkdirs();
 }
}

}

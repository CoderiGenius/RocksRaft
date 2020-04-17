package core;

import entity.RocksBatch;
import org.apache.commons.io.FilenameUtils;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.TransactionDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static RocksDB rocksDB;
    TransactionDB transactionDB;
    private final static String LOG_PATH = NodeImpl.getNodeImple().getOptions().getCurrentNodeOptions().getLogStoragePath();
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
        LOG.info("Start to init RocksDBStorageImpl on logPath {}",LOG_PATH);

        Options options = new Options();
        options.setCreateIfMissing(true);

        try {
            rocksDB = RocksDB.open(options, LOG_PATH);
            return true;
        } catch (RocksDBException e) {
            LOG.error("***Fatal error*** rocksDB open error {}",e.getMessage());
            return false;
        }
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
}

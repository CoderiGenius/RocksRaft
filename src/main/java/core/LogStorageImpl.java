package core;

import config.LogStorageOptions;
import entity.LogEntry;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import storage.LogStorage;

import java.util.List;

/**
 * Created by 周思成 on  2020/4/8 0:26
 */

public class LogStorageImpl implements LogStorage {

    LogStorageImpl(LogStorageOptions logStorageOptions){
        this.logStorageOptions = logStorageOptions;
    }

    static{
        RocksDB.loadLibrary();
    }
    private static final Logger LOG                    = LoggerFactory
            .getLogger(LogStorageOptions.class);
    static RocksDB rocksDB;
    LogStorageOptions logStorageOptions;



    @Override
    public boolean init()  {
        String path = getLogStorageOptions().getLogStoragePath()+getLogStorageOptions().getLogStorageName();
        Options options = new Options();
        options.setCreateIfMissing(true);
        try {
            rocksDB = RocksDB.open(options, path);
        } catch (RocksDBException e) {
            LOG.error("Create rocksDB connection error {}",e.getMessage());
            return false;
        }
        LOG.info("Create rocksDB connection success");
        return true;
    }



    @Override
    public long getFirstLogIndex() {
        return 0;
    }

    @Override
    public long getLastLogIndex() {
        return 0;
    }

    @Override
    public LogEntry getEntry(long index) {
        return null;
    }

    @Override
    public long getTerm(long index) {
        return 0;
    }

    @Override
    public boolean appendEntry(LogEntry entry) {
        return false;
    }

    @Override
    public int appendEntries(List<LogEntry> entries) {
        return 0;
    }

    @Override
    public boolean truncatePrefix(long firstIndexKept) {
        return false;
    }

    @Override
    public boolean truncateSuffix(long lastIndexKept) {
        return false;
    }

    @Override
    public boolean reset(long nextLogIndex) {
        return false;
    }

    public LogStorageOptions getLogStorageOptions() {
        return logStorageOptions;
    }

    public void setLogStorageOptions(LogStorageOptions logStorageOptions) {
        this.logStorageOptions = logStorageOptions;
    }
    public static RocksDB getRocksDB() {
        return rocksDB;
    }
}

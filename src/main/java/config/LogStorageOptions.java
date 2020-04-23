package config;

import core.LogManagerImpl;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/4/7 20:53
 */

public class LogStorageOptions {
    private static final Logger LOG                    = LoggerFactory
            .getLogger(LogStorageOptions.class);


        private String logStoragePath;
        private String logStorageName;

    public LogStorageOptions(String logStoragePath, String logStorageName) {
        this.logStoragePath = logStoragePath;
        this.logStorageName = logStorageName;
    }

    public String getLogStoragePath() {
        return logStoragePath;
    }

    public void setLogStoragePath(String logStoragePath) {
        this.logStoragePath = logStoragePath;
    }

    public String getLogStorageName() {
        return logStorageName;
    }

    public void setLogStorageName(String logStorageName) {
        this.logStorageName = logStorageName;
    }


}

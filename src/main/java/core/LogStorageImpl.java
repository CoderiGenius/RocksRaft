package core;

import config.LogStorageOptions;
import entity.LogEntry;
import entity.LogEntryEncoder;
import entity.LogId;
import entity.RocksBatch;
import exceptions.RaftException;
import org.rocksdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.LogEntryV2CodecFactory;
import storage.LogStorage;
import utils.Bits;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/4/8 0:26
 */

public class LogStorageImpl implements LogStorage {

    private final RocksDBStorage rocksDBStorage = RocksDBStorageImpl.getRocksDBStorage();
    private ColumnFamilyHandle defaultHandle;
    private WriteOptions                    writeOptions;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock      = this.readWriteLock.readLock();
    private final Lock                      writeLock     = this.readWriteLock.writeLock();

    LogStorageImpl(LogStorageOptions logStorageOptions){
        this.logStorageOptions = logStorageOptions;
    }
    private LogEntryEncoder logEntryEncoder;
    static{
        RocksDB.loadLibrary();
    }
    private static final Logger LOG                    = LoggerFactory
            .getLogger(LogStorageOptions.class);

    LogStorageOptions logStorageOptions;



    @Override
    public boolean init()  {
//        String path = getLogStorageOptions().getLogStoragePath()+getLogStorageOptions().getLogStorageName();
//        Options options = new Options();
        this.logEntryEncoder = LogEntryV2CodecFactory.getInstance().encoder();
        //options.setCreateIfMissing(true);
        this.writeOptions = new WriteOptions();
        this.writeOptions.setSync(true);
//        try {
//            rocksDB = RocksDB.open(options, path);
//        } catch (RocksDBException e) {
//            LOG.error("Create rocksDB connection error {}",e.getMessage());
//            return false;
//        }
//        LOG.info("Create rocksDB connection success");
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
        LogEntry logEntry = new LogEntry();
        LogId logId = new LogId();
        logId.setIndex(index);
        try {
            logEntry.setId(logId);
            logEntry.setData(ByteBuffer.wrap(rocksDBStorage.get(("log:" + index).getBytes())));

            LOG.debug("Test get key:{},value:{}",
                    index,new String(rocksDBStorage.get(("log:"+index).getBytes())
                            , StandardCharsets.UTF_8));

          return  logEntry;
        } catch (Exception e) {
            LOG.error("getEntry from rocksDB error {}",e.getMessage());
            e.printStackTrace();
        }
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
        if (entries == null || entries.isEmpty()) {
            return 0;
        }

        final int entriesCount = entries.size();
        final boolean ret = executeBatch(batch -> {
            for (int i = 0; i < entriesCount; i++) {
                final LogEntry entry = entries.get(i);
                    addDataBatch(entry, batch);
            }
        });

        if (ret) {
            return entriesCount;
        } else {
            return 0;
        }

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

    private void addDataBatch(final LogEntry entry, final WriteBatch writeBatch) throws RaftException, RocksDBException {
        final long logIndex = entry.getId().getIndex();
        final byte[] content = this.logEntryEncoder.encode(entry);
        //writeBatch.put(this.defaultHandle, getKeyBytes(logIndex),content);
        LOG.debug("RocksDB put:key:{} value:{} length:{}",logIndex,new String(content),content.length);
        rocksDBStorage.put(("log:"+logIndex).getBytes(),content);
        LOG.debug("Test get from RocksDB key:{},value:{} length:{}",
                logIndex,new String(rocksDBStorage.get(("log:"+logIndex).getBytes())
                        , StandardCharsets.UTF_8),rocksDBStorage.get(("log:"+logIndex).getBytes()).length);
    }

    public void setLogStorageOptions(LogStorageOptions logStorageOptions) {
        this.logStorageOptions = logStorageOptions;
    }


    protected byte[] getKeyBytes(final long index) {
        final byte[] ks = new byte[8];
        Bits.putLong(ks, 0, index);
        return ks;
    }

    /**
     * Execute write batch template.
     *
     * @param template write batch template
     */
    private boolean executeBatch(final WriteBatchTemplate template) {
        this.readLock.lock();
        try (final WriteBatch batch = new WriteBatch()) {
            template.execute(batch);
            //this.rocksDBStorage.write(this.writeOptions, batch);
        } catch (final RocksDBException e) {
            LOG.error("Execute batch failed with rocksdb exception.", e);
            return false;
        } catch (final IOException | RaftException e) {
            LOG.error("Execute batch failed with io exception.", e);
            return false;
        } finally {
            this.readLock.unlock();
        }
        return true;
    }
    /**
     * Write batch template.
     *
     * @author boyan (boyan@alibaba-inc.com)
     *
     * 2017-Nov-08 11:19:22 AM
     */
    private interface WriteBatchTemplate {

        void execute(WriteBatch batch) throws RocksDBException, IOException, RaftException;
    }
}

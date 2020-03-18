package service;

import config.RaftOptions;
import entity.LogEntryCodecFactory;
import storage.LogStorage;

/**
 * Created by 周思成 on  2020/3/13 11:57
 */

public interface RaftServiceFactory {

    /**
     * Creates a raft log storage.
     * @param uri  The log storage uri from {@link NodeOptions}
     * @param raftOptions  the raft options.
     * @return storage to store raft log entires.
     */
    LogStorage createLogStorage(final String uri, final RaftOptions raftOptions);


    /**
     * Creates a log entry codec factory.
     * @return a codec factory to create encoder/decoder for raft log entry.
     */
    LogEntryCodecFactory createLogEntryCodecFactory();
}

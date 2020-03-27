package service;

import config.RaftOptionsLoader;
import entity.LogEntryCodecFactory;
import storage.LogStorage;

/**
 * Created by 周思成 on  2020/3/13 14:27
 */

public class RaftServiceFactoryImpl implements RaftServiceFactory {
    @Override
    public LogStorage createLogStorage(String uri, RaftOptionsLoader raftOptions) {
        return null;
    }

    @Override
    public LogEntryCodecFactory createLogEntryCodecFactory() {
        return null;
    }
}

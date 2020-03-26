package core;

import entity.NodeOptions;

import java.util.concurrent.ThreadFactory;

/**
 * Created by 周思成 on  2020/3/24 23:46
 */

public class HeartbeatThreadFactory implements ThreadFactory {

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r,"Raft timeout thread");
    }
}

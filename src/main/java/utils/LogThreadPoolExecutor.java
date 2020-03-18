package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by 周思成 on  2020/3/13 22:15
 */

public class LogThreadPoolExecutor extends ThreadPoolExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(LogThreadPoolExecutor.class);

    private final String        name;

    public LogThreadPoolExecutor(int coreThreads, int maximumThreads
            , long keepAliveSeconds, TimeUnit unit, BlockingQueue<Runnable> workQueue
            , ThreadFactory threadFactory, RejectedExecutionHandler handler, String poolName) {

        super(coreThreads,maximumThreads,keepAliveSeconds,unit,workQueue,threadFactory,handler);
        this.name = poolName;
    }
}

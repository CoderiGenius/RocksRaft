package entity;

import core.HeartbeatThreadFactory;
import core.NodeImpl;
import core.RaftGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by 周思成 on  2020/3/25 0:01
 */

public class Heartbeat {

    private static final Logger LOG     = LoggerFactory.getLogger(RaftGroupService.class);
   private ThreadPoolExecutor threadPoolExecutor;

   private volatile TimeOutChecker checker;

   private Lock lock = new ReentrantLock(true);
    public Heartbeat(int corePoolSize, int maximumPoolSize
            , int keepAliveTime, TimeUnit timeUnit, BlockingQueue<Runnable> queue
            , ThreadFactory threadFactory, RejectedExecutionHandler rejectedExecutionHandler){
        LOG.info("start to create heartbeat module with parameters:corePoolSize:${}" +
                ",maximumPoolSize:${},keepAliveTime:${},timeUnit:${}",corePoolSize
                ,maximumPoolSize,keepAliveTime,timeUnit);
        //线程池 负责复用计时器，来判断是否超时启动leader选举
        this.threadPoolExecutor = new ThreadPoolExecutor(corePoolSize
                ,maximumPoolSize,keepAliveTime,timeUnit,queue,threadFactory,rejectedExecutionHandler);

        NodeImpl.getNodeImple().setHeartbeat(this);

    }

    public TimeOutChecker getChecker() {

        return checker;
    }

    public void setChecker(TimeOutChecker checker) {
        lock.lock();
        try {
            NodeImpl.getNodeImple().setLastReceiveHeartbeatTime(checker.getEnterQueueTime());
            this.checker = checker;
            this.getThreadPoolExecutor().execute(checker);
        } catch (Exception e) {
            LOG.error("set Heartbeat checker error:"+e.getMessage());
        }finally {
            lock.unlock();
        }

    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    public Lock getLock() {
        return lock;
    }


}

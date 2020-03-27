package entity;

import core.NodeImpl;
import core.RaftGroupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ElectionService;
import utils.Utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

/**
 * Created by 周思成 on  2020/3/25 12:36
 */

public class TimeOutChecker implements Runnable{

    private static final Logger LOG     = LoggerFactory.getLogger(RaftGroupService.class);

    /**
     * 超时时间
     */
    private long timeOut;

    /**
     * 进入线程池的时间
     */
    private long enterQueueTime;

    public TimeOutChecker(long timeOut,long enterQueueTime) {

        this.enterQueueTime = enterQueueTime;
        this.timeOut = timeOut;
    }

    @Override
    public void run() {



        NodeImpl.getNodeImple().getHeartbeat().getLock().lock();
        synchronized(NodeImpl.getNodeImple().getHeartbeat().getChecker()) {
            try {
                //检查开始运行的时间，是否已经超过timeout时间，如果超出则直接检查
                long currentTimeDifferent = Utils.monotonicMs() - enterQueueTime;
                LOG.debug("Utils.monotonicMs():"+Utils.monotonicMs());
                LOG.debug("enterQueueTime:"+enterQueueTime);
                if (currentTimeDifferent > timeOut) {
                    //在队列里等待的时候就已经超时了，检查node是否超时
                    LOG.error("Timeout during waiting int queue");
                    checkNodeStatus();
                } else {
                    //还未超时，等待检测超时
                    try {
                        LOG.debug("thread wait:"+(currentTimeDifferent));
                        NodeImpl.getNodeImple().getHeartbeat().getChecker().wait(timeOut - currentTimeDifferent);

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    checkNodeStatus();

                }
            } catch (Exception e) {
                LOG.error("Heartbeat timeout checker exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                //NodeImpl.getNodeImple().getHeartbeat().getLock().unlock();
            }


        }

    }

    private void checkNodeStatus()  {

        if ((Utils.monotonicMs()
                - NodeImpl.getNodeImple().getLastReceiveHeartbeatTime().get()) >= timeOut) {
            //超时，执行超时逻辑
            LOG.error("Node timeout, start to launch ElectionService");
            ElectionService.checkToStartElection();
        }else {
            //未超时
        }
    }
}

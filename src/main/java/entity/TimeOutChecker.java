package entity;

import core.NodeImpl;
import service.ElectionService;

/**
 * Created by 周思成 on  2020/3/25 12:36
 */

public class TimeOutChecker implements Runnable{

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



        //检查开始运行的时间，是否已经超过timeout时间，如果超出则直接检查
        long currentTimeDifferent = System.currentTimeMillis() - enterQueueTime;

        if(currentTimeDifferent>timeOut){
                //在队列里等待的时候就已经超时了，检查node是否超时
        }else {
            //还未超时，等待检测超时
            try {
                this.wait(timeOut - currentTimeDifferent);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }


        NodeImpl.getNodeImple().getLastReceiveHeartbeatTime()
    }

    private void checkNodeStatus() throws ClassNotFoundException {

        if ((System.currentTimeMillis()
                - NodeImpl.getNodeImple().getLastReceiveHeartbeatTime().get()) >= timeOut) {
            //超时，执行超时逻辑

            ElectionService electionService = Class.forName("ElectionService");
        }else {
            //未超时
        }
    }
}

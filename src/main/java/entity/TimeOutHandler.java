package entity;

import utils.RandomTimeUtil;

/**
 * Created by 周思成 on  2020/3/25 13:19
 */

public class TimeOutHandler implements TimeOutCallBack {


    @Override
    public void timeOuted() {
        //超时，随机时间开始选举
        //随机时间开始选举
        long randomElectionTime = RandomTimeUtil
                .newRandomTime(NodeOptions.getNodeOptions().getMaxHeartBeatTime()
                        ,NodeOptions.getNodeOptions().getMaxElectionTime());
        try {
            Thread.sleep(randomElectionTime);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

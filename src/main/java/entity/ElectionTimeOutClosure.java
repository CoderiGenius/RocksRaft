package entity;

import core.NodeImpl;
import service.ElectionService;

/**
 * Created by 周思成 on  2020/4/1 16:10
 */

public class ElectionTimeOutClosure implements Closure,TimeOutClosure {
    @Override
    public void run(Status status) {
        ElectionService.checkToStartPreVote();
    }


    @Override
    public long getBaseTime(){
        return NodeImpl.getNodeImple().getLastReceiveHeartbeatTime().longValue();
    }
}

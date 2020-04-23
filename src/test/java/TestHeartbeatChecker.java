import com.alipay.sofa.rpc.common.annotation.JustForTest;

/**
 * Created by 周思成 on  2020/4/23 0:45
 */
import core.HeartbeatThreadFactory;
import entity.ElectionTimeOutClosure;
import entity.Heartbeat;
import entity.TimeOutChecker;
import org.junit.Test;
import utils.Utils;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestHeartbeatChecker {

    @Test
    public void testHeartbeat() {
        Heartbeat heartbeat = new Heartbeat(1, 2
                , 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>()
                , new HeartbeatThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());

        heartbeat.setChecker(
                new TimeOutChecker( Utils.monotonicMs(), new ElectionTimeOutClosure()));
    }
}

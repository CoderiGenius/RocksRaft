//import com.alipay.sofa.rpc.common.annotation.JustForTest;
//
///**
// * Created by 周思成 on  2020/4/23 0:45
// */
//import core.HeartbeatThreadFactory;
//import core.NodeImpl;
//import entity.ElectionTimeOutClosure;
//import entity.Heartbeat;
//import entity.TimeOutChecker;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import utils.Utils;
//
//import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicLong;
//
//public class TestHeartbeatChecker {
//    public static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);
//
//    @Test
//    public void testHeartbeat() {
////        Heartbeat heartbeat = new Heartbeat(1, 2
////                , 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>()
////                , new HeartbeatThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());
////
////        heartbeat.setChecker(
////                new TimeOutChecker( Utils.monotonicMs(), new ElectionTimeOutClosure(),""));
//
//        LOG.debug("123");
//        LOG.info("123123");
//        LOG.error("123123123");
//        LOG.warn("12312312312312");
//        AtomicLong atomicLong = new AtomicLong(3/2+1);
//        LOG.info(atomicLong.get()+"");
//    }
//}

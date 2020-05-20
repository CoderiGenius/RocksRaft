//import com.google.protobuf.ZeroByteStringHelper;
//import org.junit.Test;
//import rpc.RpcRequests;
//import utils.TimerManager;
//
//import java.nio.ByteBuffer;
//import java.util.concurrent.TimeUnit;
//
///**
// * Created by 周思成 on  2020/4/23 18:41
// */
//
//public class TestTimeManager {
//
//    TimerManager timerManager = new TimerManager();
//    @Test
//    public void Test() throws InterruptedException {
////        Runnable runnable = () -> {
////            System.out.println(123);
////            try {
////                Test();
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////            //timerManager.schedule(runnable,1000, TimeUnit.MILLISECONDS);
////        }
////        ;
//        RpcRequests.AppendEntriesRequest.Builder builder = RpcRequests.AppendEntriesRequest.newBuilder();
//        ByteBuffer byteBuffer = ByteBuffer.wrap("123".getBytes());
//        ByteBuffer byteBuffer1 = null;
//        builder.setData(ZeroByteStringHelper.wrap(byteBuffer1));
//        RpcRequests.AppendEntriesRequest appendEntriesRequest = builder.build();
//        String t = new String(ZeroByteStringHelper.getByteArray(appendEntriesRequest.getData()));
//        System.out.println(t);
//
////        timerManager.init(100);
////        timerManager.schedule(runnable,1000, TimeUnit.MILLISECONDS);
////        Thread.currentThread().join();
//    }
//}

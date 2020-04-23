import core.RaftGroupService;
import entity.Node;
import entity.NodeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/13 23:32
 */

public class Start2 {
    private static final Logger LOG     = LoggerFactory.getLogger(Start2.class);
    public static void main(String[] args) throws InterruptedException {




        Runnable runnable = () -> {
            NodeOptions nodeOptions =  NodeOptions.getNodeOptions();

            RaftGroupService raftGroupService = new RaftGroupService( nodeOptions,"E:\\NewJavaEEWorkplace\\RocksRaft\\src\\main\\resources\\properties.yml");
            try {
                Node node = raftGroupService.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);


        Runnable runnable2 = () -> {
            NodeOptions nodeOptions2 =  NodeOptions.getNodeOptions();

            RaftGroupService raftGroupService2 = new RaftGroupService( nodeOptions2,"E:\\NewJavaEEWorkplace\\RocksRaft\\src\\main\\resources\\properties2.yml");
            try {
                Node node2 = raftGroupService2.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread2 = new Thread(runnable2);


        Runnable runnable3 = () -> {
            NodeOptions nodeOptions3 =  NodeOptions.getNodeOptions();

            RaftGroupService raftGroupService3 = new RaftGroupService( nodeOptions3,"E:\\NewJavaEEWorkplace\\RocksRaft\\src\\main\\resources\\properties3.yml");
            try {
                Node node3 = raftGroupService3.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread3 = new Thread(runnable3);

        //thread.start();
        thread2.start();
        //thread3.start();

        Thread.currentThread().join();









//// 使用 node 提交任务
//        Task task = ....
//        node.apply(task);
    }
}

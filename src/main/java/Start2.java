import core.RaftGroupService;
import entity.Node;
import entity.NodeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/13 23:32
 */

public class Start2 {
    public static void main(String[] args) throws InterruptedException {



        Runnable runnable2 = () -> {
            NodeOptions nodeOptions2 =  NodeOptions.getNodeOptions();

            RaftGroupService raftGroupService2 = new RaftGroupService( nodeOptions2,Start.class.getResource("properties2.yml").getPath());
            try {
                Node node2 = raftGroupService2.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread2 = new Thread(runnable2);

        thread2.start();

        Thread.currentThread().join();









//// 使用 node 提交任务
//        Task task = ....
//        node.apply(task);
    }
}

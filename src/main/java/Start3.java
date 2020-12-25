import core.RaftGroupService;
import entity.Node;
import entity.NodeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/13 23:32
 */

public class Start3 {
    public static void main(String[] args) throws InterruptedException {


        Runnable runnable3 = () -> {
            NodeOptions nodeOptions3 =  NodeOptions.getNodeOptions();

            RaftGroupService raftGroupService3 = new RaftGroupService( nodeOptions3,Start.class.getResource("properties3.yml").getPath());
            try {
                Node node3 = raftGroupService3.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread3 = new Thread(runnable3);

        thread3.start();

        Thread.currentThread().join();









//// 使用 node 提交任务
//        Task task = ....
//        node.apply(task);
    }
}

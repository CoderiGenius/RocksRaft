import core.NodeImpl;
import core.RaftGroupService;
import entity.Endpoint;
import entity.Node;
import entity.NodeOptions;
import entity.PeerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/13 23:32
 */

public class Start {
    public static void main(String[] args) throws InterruptedException {


        Runnable runnable = () -> {
            NodeOptions nodeOptions =  NodeOptions.getNodeOptions();

            RaftGroupService raftGroupService =
                    new RaftGroupService( nodeOptions
                            ,Start.class.getResource("properties.yml").getPath());

            try {
                Node node = raftGroupService.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(runnable);

        thread.start();


        Thread.currentThread().join();



    }
}

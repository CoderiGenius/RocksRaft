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
    private static final Logger LOG     = LoggerFactory.getLogger(Start.class);
    public static void main(String[] args) {

        NodeOptions nodeOptions =  NodeOptions.getNodeOptions();

        RaftGroupService cluster = new RaftGroupService( nodeOptions,"E:\\NewJavaEEWorkplace\\RocksRaft\\src\\main\\resources\\properties.yml");
        Node node = cluster.start();

//// 使用 node 提交任务
//        Task task = ....
//        node.apply(task);
    }
}

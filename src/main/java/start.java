import core.RaftGroupService;
import entity.Endpoint;
import entity.Node;
import entity.NodeOptions;
import entity.PeerId;

/**
 * Created by 周思成 on  2020/3/13 23:32
 */

public class start {

    public static void main(String[] args) {
        String groupId = "jraft";
        Endpoint endpoint = new Endpoint("localhost",8800);
        PeerId serverId = new PeerId();
        serverId.setEndpoint(endpoint);
        NodeOptions nodeOptions =  NodeOptions.getNodeOptions();

        RaftGroupService cluster = new RaftGroupService(groupId, serverId, nodeOptions);
        Node node = cluster.start();

// 使用 node 提交任务
        Task task = ....
        node.apply(task);
    }
}

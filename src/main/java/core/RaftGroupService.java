package core;

import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.util.StringUtils;
import entity.Endpoint;
import entity.Node;
import entity.NodeOptions;
import entity.PeerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

/**
 * Created by 周思成 on  2020/3/13 23:38
 * @author Mike
 */

public class RaftGroupService {

    private static final Logger LOG     = LoggerFactory.getLogger(RaftGroupService.class);

    static{
        //加载rpc protobuf
    }
    private volatile boolean    started = false;

    /**
     * This node serverId
     */
    private PeerId serverId;

    /**
     * Node options
     */
    private NodeOptions nodeOptions;

    /**
     * The raft RPC server
     */
    private RpcServer rpcServer;



    /**
     * The raft group id
     */
    private String              groupId;
    /**
     * The raft node.
     */
    private Node node;

   public RaftGroupService(String groupId,PeerId peerId,NodeOptions nodeOptions){
        this.groupId = groupId;
        this.serverId = peerId;
        this.nodeOptions = nodeOptions;
   }

   public RaftGroupService(String groupId,PeerId peerId,NodeOptions nodeOptions,)


   public Node start(){
       if (this.started) {
           return this.node;
       }
       if (this.serverId == null || this.serverId.getEndpoint() == null
               || this.serverId.getEndpoint().equals(new Endpoint(Utils.IP_ANY, 0))) {
           throw new IllegalArgumentException("Blank serverId:" + this.serverId);
       }
       if (StringUtils.isBlank(this.groupId)) {
           throw new IllegalArgumentException("Blank group id" + this.groupId);
       }
        //注册rpc
       NodeManagerImpl.getInstance().addAddress(this.serverId.getEndpoint());
   }
}

package core;

import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.util.StringUtils;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import entity.Endpoint;
import entity.Node;
import entity.NodeOptions;
import entity.PeerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.RandomTimeUtil;
import utils.Utils;

import java.lang.reflect.Type;

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

    public RaftGroupService(String groupId, PeerId peerId, NodeOptions nodeOptions, RpcServer rpcServer) {
        this.groupId = groupId;
        this.serverId = peerId;
        this.nodeOptions = nodeOptions;
        this.rpcServer = rpcServer;

    }


   public Node start() throws ClassNotFoundException {
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
       //NodeManagerImpl.getInstance().addAddress(this.serverId.getEndpoint());

       //开启rpc
       ServerConfig serverConfig = new ServerConfig()
               .setProtocol(nodeOptions.getRpcProtocol())
               .setSerialization(nodeOptions.getSerialization())
               .setPort(nodeOptions.getPort())
               .setDaemon(nodeOptions.isDaemon());


       //随机时间开始选举
      long randomElectionTime = RandomTimeUtil
              .newRandomTime(nodeOptions.getMaxHeartBeatTime(),nodeOptions.getMaxElectionTime());


   }
}

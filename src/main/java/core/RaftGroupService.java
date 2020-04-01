package core;

import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.util.StringUtils;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import config.RaftOptionsLoader;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcResponseClosure;
import rpc.RpcServices;
import rpc.RpcServicesImpl;
import utils.RandomTimeUtil;
import utils.Utils;

import java.io.FileNotFoundException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

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
    private PeerId peerId;

    /**
     * Node options
     */
    private NodeOptions nodeOptions;

    /**
     * The raft RPC server
     */
    private RpcServer rpcServer;



    private Heartbeat heartbeat;

    /**
     * The raft group id
     */
    private String              groupId;
    /**
     * The raft node.
     */
    private Node node;

   public RaftGroupService(NodeOptions nodeOptions,String configurationPath){

        this.nodeOptions = nodeOptions;
       try {
           new RaftOptionsLoader(configurationPath);
       } catch (FileNotFoundException e) {
           LOG.error("Configuration not found path:"+configurationPath);
           e.printStackTrace();
       }
       this.peerId = NodeImpl.getNodeImple().getNodeId().getPeerId();
       this.groupId = NodeImpl.getNodeImple().getNodeId().getGroupId();
   }

    public RaftGroupService(String groupId, PeerId peerId, NodeOptions nodeOptions, RpcServer rpcServer) {
        this.groupId = groupId;
        this.peerId = peerId;
        this.nodeOptions = nodeOptions;
        this.rpcServer = rpcServer;

//        //超时检测线程池
//            this.heartbeat = new Heartbeat(1
//            ,2,0
//            , TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>()
//            ,new HeartbeatThreadFactory(),new ThreadPoolExecutor.DiscardPolicy());
//            //放入超时检测线程
//
//        this.heartbeat.getThreadPoolExecutor().execute();

   }


   public Node start()  {
       if (this.started) {
           return this.node;
       }
       if (this.peerId == null || this.peerId.getEndpoint() == null
               || this.peerId.getEndpoint().equals(new Endpoint(Utils.IP_ANY, 0))) {
           throw new IllegalArgumentException("Blank peerId:" + this.peerId);
       }
       if (StringUtils.isBlank(this.groupId)) {
           throw new IllegalArgumentException("Blank group id" + this.groupId);
       }

       NodeImpl node = NodeImpl.getNodeImple();

       //start rpc service
       ServerConfig serverConfig = new ServerConfig()
               .setProtocol(nodeOptions.getRpcProtocol())
               .setSerialization(nodeOptions.getSerialization())
               .setPort(nodeOptions.getPort())
               .setDaemon(nodeOptions.isDaemon());

       ProviderConfig<RpcServices> providerConfig = new ProviderConfig<RpcServices>()
               .setInterfaceId(RpcServices.class.getName())
               .setRef(new RpcServicesImpl())
               .setServer(serverConfig);
       providerConfig.export();

        //save the proxy class in to list
       for (PeerId p:node.getPeerIdList()
            ) {

           ConsumerConfig<RpcServices> consumerConfig = new ConsumerConfig<RpcServices>()
                   .setInvokeType("callback")
                   .setOnReturn(new RpcResponseClosure())
                   .setProtocol(nodeOptions.getRpcProtocol())
                   .setDirectUrl(nodeOptions.getRpcProtocol()
                           +"://"+p.getEndpoint().getIp()+":"+p.getEndpoint().getPort())
                   .setInterfaceId(RpcServices.class.getName());

           node.getRpcServicesMap().put(p.getEndpoint(),consumerConfig.refer());
       }



       //heartbeat
       Heartbeat heartbeat = new Heartbeat(1,2
               ,0,TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>()
               ,new HeartbeatThreadFactory(),new ThreadPoolExecutor.DiscardPolicy());

       heartbeat.setChecker(
               new TimeOutChecker(NodeOptions.getNodeOptions().getMaxHeartBeatTime(),Utils.monotonicMs(),new ElectionTimeOutClosure()));


       return node;
   }


}

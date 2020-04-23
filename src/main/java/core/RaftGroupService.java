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
import rpc.*;
import utils.Utils;

import java.io.FileNotFoundException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 周思成 on  2020/3/13 23:38
 *
 * @author Mike
 */

public class RaftGroupService {

    private static final Logger LOG = LoggerFactory.getLogger(RaftGroupService.class);

    static {
        //加载rpc protobuf
    }

    private volatile boolean started = false;

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
    private String groupId;
    /**
     * The raft node.
     */
    private Node node;

    public RaftGroupService(NodeOptions nodeOptions, String configurationPath) {

        this.nodeOptions = nodeOptions;
        try {
            new RaftOptionsLoader(configurationPath);
        } catch (FileNotFoundException e) {
            LOG.error("Configuration not found path:" + configurationPath);
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


    public Node start() throws InterruptedException {
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
        node.init();

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

        //start TaskRpc service
        ServerConfig serverConfigForTasks = new ServerConfig()
                .setProtocol(nodeOptions.getRpcProtocol())
                //.setSerialization(nodeOptions.getSerialization())
                .setPort(nodeOptions.getTaskPort())
                .setDaemon(nodeOptions.isDaemon());

        ProviderConfig<TaskServicesImpl> providerConfigForTasks = new ProviderConfig<TaskServicesImpl>()
                .setInterfaceId(TaskRpcServices.class.getName())
                .setRef(new TaskServicesImpl())
                .setServer(serverConfigForTasks);
        providerConfigForTasks.export();

        //save the proxy class in to list
        for (PeerId p : node.getPeerIdList()
        ) {
            ConsumerConfig<RpcServicesImpl> consumerConfig;
            consumerConfig = new ConsumerConfig<RpcServicesImpl>()
                    .setInvokeType("callback")
                    .setOnReturn(new RpcResponseClosure())
                    .setProtocol(nodeOptions.getRpcProtocol())
                    .setDirectUrl(nodeOptions.getRpcProtocol()
                            + "://" + p.getEndpoint().getIp() + ":" + p.getEndpoint().getPort())
                    .setInterfaceId(RpcServices.class.getName())
                    .setSerialization(nodeOptions.getSerialization());
            node.getRpcServicesMap().put(p.getEndpoint(), consumerConfig.refer());


            ConsumerConfig<TaskServicesImpl> consumerConfigForTasks;
            if ("callback".equals(nodeOptions.getTaskExecuteMethod())) {
                consumerConfigForTasks = new ConsumerConfig<TaskServicesImpl>()
                        .setInvokeType(nodeOptions.getTaskExecuteMethod())
                        .setOnReturn(new TaskRpcResponseClosure())
                        .setProtocol(nodeOptions.getRpcProtocol())
                        .setDirectUrl(nodeOptions.getRpcProtocol()
                                + "://" + p.getEndpoint().getIp() + ":" + p.getTaskPort())
                        .setInterfaceId(TaskRpcServices.class.getName());
            } else {
                consumerConfigForTasks = new ConsumerConfig<TaskServicesImpl>()
                        .setProtocol(nodeOptions.getRpcProtocol())
                        .setDirectUrl(nodeOptions.getRpcProtocol()
                                + "://" + p.getEndpoint().getIp() + ":" + p.getTaskPort())
                        .setInterfaceId(TaskRpcServices.class.getName());
            }
            node.getTaskRpcServices().put(p.getEndpoint(), consumerConfigForTasks.refer());
        }


        //heartbeat
        Heartbeat heartbeat = new Heartbeat(1, 20
                , 0, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>()
                , new HeartbeatThreadFactory(), new ThreadPoolExecutor.DiscardPolicy());


        Thread.sleep(5000);
        heartbeat.setChecker(
                new TimeOutChecker( Utils.monotonicMs(), new ElectionTimeOutClosure()));
        LOG.info("Add initial Heartbeat");

        return node;
    }


}

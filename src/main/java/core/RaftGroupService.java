package core;


import com.alipay.sofa.rpc.common.utils.StringUtils;
import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import config.RaftOptionsLoader;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.*;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
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

    public RaftGroupService(String groupId, PeerId peerId, NodeOptions nodeOptions) {
        this.groupId = groupId;
        this.peerId = peerId;
        this.nodeOptions = nodeOptions;




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
            ConsumerConfig<RpcServices> consumerConfig;
            consumerConfig = new ConsumerConfig<RpcServices>()
                    .setInvokeType("callback")
                    .setOnReturn(new RpcResponseClosure())
                    .setProtocol(nodeOptions.getRpcProtocol())
                    .setDirectUrl(nodeOptions.getRpcProtocol()
                            + "://" + p.getEndpoint().getIp() + ":" + p.getEndpoint().getPort())
                    .setInterfaceId(RpcServices.class.getName())
                    .setSerialization(nodeOptions.getSerialization());
            node.getRpcServicesMap().put(p.getEndpoint(), consumerConfig.refer());


            ConsumerConfig<TaskRpcServices> consumerConfigForTasks;
            if ("callback".equals(nodeOptions.getTaskExecuteMethod())) {
                consumerConfigForTasks = new ConsumerConfig<TaskRpcServices>()
                        .setInvokeType(nodeOptions.getTaskExecuteMethod())
                        .setOnReturn(new TaskRpcResponseClosure())
                        .setProtocol(nodeOptions.getRpcProtocol())
                        .setDirectUrl(nodeOptions.getRpcProtocol()
                                + "://" + p.getEndpoint().getIp() + ":" + p.getTaskPort())
                        .setInterfaceId(TaskRpcServices.class.getName());
            } else {
                consumerConfigForTasks = new ConsumerConfig<TaskRpcServices>()
                        .setProtocol(nodeOptions.getRpcProtocol())
                        .setDirectUrl(nodeOptions.getRpcProtocol()
                                + "://" + p.getEndpoint().getIp() + ":" + p.getTaskPort())
                        .setInterfaceId(TaskRpcServices.class.getName());
            }
            TaskRpcServices taskRpcServices = consumerConfigForTasks.refer();
            node.getTaskRpcServices().put(p.getEndpoint(), taskRpcServices);

            LOG.debug("Add task rpc service {},{}",p.getEndpoint(),node.getTaskRpcServices().get(p.getEndpoint()));
            LOG.debug("Add task rpc service {}",consumerConfigForTasks.getDirectUrl());



            //Set client task rpc service for notifying the client the result of read index
            ConsumerConfig<ClientRpcService> readConsumerConfig = new ConsumerConfig<ClientRpcService>()
                    .setInterfaceId(ClientRpcService.class.getName())
                    .setProtocol("bolt")
                    .setConnectTimeout(2000)
                    .setReconnectPeriod(1000)
                    .setRetries(100)
                    .setDirectUrl("bolt"+"://"+nodeOptions.getClientAddress()+":"+nodeOptions.getClientPort());

            NodeImpl.getNodeImple().setClientRpcService(readConsumerConfig.refer());
        }

        node.init();
        return node;
    }


}

package rpc;

import com.alipay.remoting.rpc.RpcServer;
import entity.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

/**
 * Created by 周思成 on  2020/3/18 13:03
 */

public class RaftRpcServerFactory {

    public static final Logger LOG = LoggerFactory.getLogger(RaftRpcServerFactory.class);


    /**
     * Creates a raft RPC server with executors to handle requests.
     *
     * @param endpoint      server address to bind
     * @param raftExecutor  executor to handle RAFT requests.
     * @param cliExecutor   executor to handle CLI service requests.
     * @return a rpc server instance
     */
    public static RpcServer createRaftRpcServer(final Endpoint endpoint, final Executor raftExecutor
            ,final Executor cliExecutor){
       final RpcServer rpcServer = new RpcServer(endpoint.getPort(),true,true);
       
    }
}

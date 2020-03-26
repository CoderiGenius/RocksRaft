package rpc;

import core.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/24 13:10
 */

public class RpcServicesImpl implements RpcServices {

    public static final Logger LOG = LoggerFactory.getLogger(RpcServicesImpl.class);

    @Override
    public RpcRequests sendRpcRequest(RpcRequests rpcRequests) {

        return null;
    }

    @Override
    public RpcRequests.RequestPreVoteResponse sendPreVoteRequest(RpcRequests.RequestPreVoteRequest requestPreVoteRequest) {
       long candidateTerm = requestPreVoteRequest.getLastLogTerm();
       long selfTerm = NodeImpl.getNodeImple().getTerm();
        RpcRequests.RequestPreVoteResponse.Builder builder = RpcRequests.RequestPreVoteResponse.newBuilder();
        if (candidateTerm < selfTerm) {
            builder.setGranted(false);
            builder.setTerm(selfTerm);
        }else{
            builder.setGranted(true);
            builder.setTerm(selfTerm);
        }
        RpcRequests.RequestPreVoteResponse requestPreVoteResponse = builder.build();
        return requestPreVoteResponse;
    }
}

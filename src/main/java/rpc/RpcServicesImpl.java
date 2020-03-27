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
    public RpcRequests.RequestPreVoteResponse handlePreVoteRequest(RpcRequests.RequestPreVoteRequest requestPreVoteRequest) {
       LOG.info("Recevice preVoteRequest from {}",requestPreVoteRequest.getPeerId());
        long candidateTerm = requestPreVoteRequest.getLastLogTerm();
        long selfTerm = NodeImpl.getNodeImple().getTerm();
        RpcRequests.RequestPreVoteResponse.Builder builder = RpcRequests.RequestPreVoteResponse.newBuilder();
        builder.setTerm(selfTerm);


       //check current node status
        if ( ! NodeImpl.getNodeImple().checkIfCurrentNodeCanVoteOthers()) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the preVote request " +
                    "as the current node status is not for voting. status:{}",NodeImpl.getNodeImple().getNodeState());
            return builder.build();
        }


        if (candidateTerm < selfTerm ) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the preVote request " +
                    "as the candidate's term {} is not as newer as the current {}",candidateTerm,selfTerm);
            return builder.build();
        } else if (NodeImpl.getNodeImple().isCurrentLeaderValid()) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the preVote request " +
                    "as the current peer leader is still valid");
            return builder.build();
        }

        //check log entries

        return builder.build();
    }
}

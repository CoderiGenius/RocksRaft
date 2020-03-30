package service;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import core.NodeImpl;
import entity.Endpoint;
import entity.PeerId;
import exceptions.ElectionException;
import rpc.RpcRequests;
import rpc.RpcServices;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by 周思成 on  2020/3/24 16:02
 */

public class ElectionServiceImpl implements ElectionService , Callable  {
    @Override
    public boolean startElection() {
            //修改当前节点状态为prevote
        NodeImpl.getNodeImple().setNodeState(NodeImpl.NodeState.candidate);
        return true;
    }

    @Override
    public void startPrevote() {
        if ( ! NodeImpl.getNodeImple().checkIfCurrentNodeCanStartPreVote()) {
            return;
        }
        NodeImpl.getNodeImple().setNodeState(NodeImpl.NodeState.preCandidate);
        RpcRequests.RequestPreVoteRequest.Builder builder = RpcRequests.RequestPreVoteRequest.newBuilder();
        builder.setLastLogTerm(NodeImpl.getNodeImple().getTerm());
        builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
        builder.setLastLogTerm(NodeImpl.getNodeImple().getLastLogTerm().longValue());
        builder.setLastLogIndex(NodeImpl.getNodeImple().getLastLogIndex().longValue());
        //send preVote request to all peers in the list
        Map<Endpoint, RpcServices> map = NodeImpl.getNodeImple().getRpcServices();
        for (PeerId p:NodeImpl.getNodeImple().getPeerIdList()
             ) {
           map.get(p.getEndpoint()).handlePreVoteRequest(builder.build());
        }
    }

    @Override
    public Object call() {
        return startElection();
    }
}

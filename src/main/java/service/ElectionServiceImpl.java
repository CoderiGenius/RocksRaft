package service;

import core.NodeImpl;
import exceptions.ElectionException;
import rpc.RpcRequests;

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
        System.out.println(NodeImpl
                .getNodeImple().getRpcServices().handlePreVoteRequest(builder.build()).getGranted());
    }

    @Override
    public Object call() {
        return startElection();
    }
}

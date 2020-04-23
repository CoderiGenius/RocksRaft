package service;

import core.NodeImpl;
import rpc.RpcRequests;

/**
 * Created by 周思成 on  2020/3/24 16:02
 */

public interface ElectionService {




    /**
     * 检查是否启动election
     */
    public static void checkToStartPreVote(){
        System.out.println("checkToStartPreVote:"+NodeImpl.getNodeImple().checkNodeStatePreCandidate());
        if ( ! NodeImpl.getNodeImple().checkNodeStatePreCandidate()) {
            ElectionService electionService = new ElectionServiceImpl();
            electionService.startPrevote();
        }
    }

    public static void checkToStartElection() {
        if (NodeImpl.getNodeImple().checkIfCurrentNodeCanStartElection()) {
            //start election
            ElectionService electionService = new ElectionServiceImpl();
            electionService.election();
        }
    }

    public void startPrevote();

    public void election();

    public void handlePrevoteResponse(RpcRequests.RequestPreVoteResponse requestPreVoteResponse);

    public void handleElectionResponse(RpcRequests.RequestVoteResponse requestVoteResponse);
}

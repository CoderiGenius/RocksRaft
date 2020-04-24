package service;

import core.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcRequests;
import rpc.RpcServicesImpl;

/**
 * Created by 周思成 on  2020/3/24 16:02
 */

public interface ElectionService {

    public static final Logger LOG = LoggerFactory.getLogger(ElectionService.class);


    /**
     * 检查是否启动election
     */
    public static void checkToStartPreVote(){
        LOG.debug("checkToStartPreVote:"+NodeImpl.getNodeImple().checkNodeStatePreCandidate());
        if ( ! NodeImpl.getNodeImple().checkNodeStatePreCandidate()) {
            ElectionService electionService = new ElectionServiceImpl();
            electionService.startPrevote();
        }
    }

    public static void checkToStartElection() {
        LOG.debug("checkToStartElection:{}",NodeImpl.getNodeImple().checkIfCurrentNodeCanStartElection());
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

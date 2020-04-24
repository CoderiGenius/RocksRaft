package service;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import core.NodeImpl;
import entity.Ballot;
import entity.Endpoint;
import entity.Node;
import entity.PeerId;
import exceptions.ElectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcRequests;
import rpc.RpcServices;
import rpc.RpcServicesImpl;

import utils.Utils;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by 周思成 on  2020/3/24 16:02
 */

public class ElectionServiceImpl implements ElectionService , Callable  {

    public static final Logger LOG = LoggerFactory.getLogger(RpcServicesImpl.class);


    @Override
    public void startPrevote() {


        if ( ! NodeImpl.getNodeImple().checkIfCurrentNodeCanStartPreVote()) {
            LOG.info("Can not start preVote " +
                    "as the current state {} is invalid",NodeImpl.getNodeImple().getNodeState());
            return;
        }

        NodeImpl.getNodeImple().getWriteLock().lock();

        try {

            NodeImpl.getNodeImple().setNodeState(NodeImpl.NodeState.preCandidate);
            NodeImpl.getNodeImple().setPreVoteBallot(
                    new Ballot(NodeImpl.getNodeImple().getPeerIdList()));
            //elect self
            //LOG.debug("Elect self {}",NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            NodeImpl.getNodeImple().getPreVoteBallot().grant(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            RpcRequests.RequestPreVoteRequest.Builder builder = RpcRequests.RequestPreVoteRequest.newBuilder();
            builder.setLastLogTerm(NodeImpl.getNodeImple().getLastLogTerm().longValue());
            builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            builder.setPeerName(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            builder.setLastLogTerm(NodeImpl.getNodeImple().getLastLogTerm().longValue());
            builder.setLastLogIndex(NodeImpl.getNodeImple().getLastLogIndex().longValue());
            RpcRequests.RequestPreVoteRequest requestPreVoteRequest = builder.build();
            //send preVote request to all peers in the list
            Map<Endpoint, RpcServices> map = NodeImpl.getNodeImple().getRpcServicesMap();

            for (PeerId p : NodeImpl.getNodeImple().getPeerIdList()
            ) {
                long t = Utils.monotonicMs();

                //map.get(p.getEndpoint()).handlePreVoteRequest(requestPreVoteRequest);
                NodeImpl.getNodeImple().getEnClosureRpcRequest()
                        .handlePreVoteRequest(requestPreVoteRequest,p.getEndpoint(),true);
                LOG.info("Send preVote request from {} to {} at {} on term {}"
                        ,requestPreVoteRequest.getPeerId(), p, t, NodeImpl.getNodeImple().getLastLogTerm());
                //LOG.info("Send preVote request check {}",requestPreVoteRequest.getSerializedSize());
            }
        } catch (Exception e) {
            LOG.info("PreVote erro {}",e.getMessage());
            e.printStackTrace();
        }finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }
    }

    @Override
    public void election() {
        LOG.info("Current node start election");
        NodeImpl.getNodeImple().getWriteLock().lock();
        try {
            NodeImpl.getNodeImple().setNodeState(NodeImpl.NodeState.candidate);
            NodeImpl.getNodeImple().setElectionBallot(new Ballot(NodeImpl.getNodeImple().getPeerIdList()));

            //do increment to term

            RpcRequests.RequestVoteRequest.Builder builder = RpcRequests.RequestVoteRequest.newBuilder();
            builder.setLastLogIndex(NodeImpl.getNodeImple().getLastLogIndex().longValue());
            builder.setLastLogTerm(NodeImpl.getNodeImple().getLastLogTerm().longValue());
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().incrementAndGet());
            builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getPeerName());
            builder.setServerId(NodeImpl.getNodeImple().getNodeId().getGroupId());
            //elect self
            NodeImpl.getNodeImple().getElectionBallot().grant(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
            NodeImpl.getNodeImple().setLastVoteTerm(NodeImpl.getNodeImple().getLastLogTerm().get());

            //send vote request to all peers in the list
            Map<Endpoint, RpcServices> map = NodeImpl.getNodeImple().getRpcServicesMap();
            RpcRequests.RequestVoteRequest requestVoteRequest = builder.build();
            for (PeerId p : NodeImpl.getNodeImple().getPeerIdList()
            ) {
                long t = Utils.monotonicMs();
                LOG.info("Send vote request to {} at {} on newTerm {}", p, t, NodeImpl.getNodeImple().getLastLogTerm());
                //map.get(p.getEndpoint()).handleVoteRequest(requestVoteRequest);
                NodeImpl.getNodeImple().getEnClosureRpcRequest()
                        .handleVoteRequest(requestVoteRequest,p.getEndpoint(),true);
            }
        } catch (Exception e) {
            LOG.error("Election failed {}",e.getMessage());
        }finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }
    }

    @Override
    public void handlePrevoteResponse(RpcRequests.RequestPreVoteResponse requestPreVoteResponse) {
        LOG.info("Handle preVote response from: {} result: {}"
                ,requestPreVoteResponse.getPeerName(),requestPreVoteResponse.getGranted());
        NodeImpl.getNodeImple().getWriteLock().lock();
        try {
            if (requestPreVoteResponse.getGranted()) {
                NodeImpl.getNodeImple().getPreVoteBallot().grant(requestPreVoteResponse.getPeerName());
            }
            LOG.debug("Prevote grant list {}",NodeImpl.getNodeImple().getPreVoteBallot().getGranted());
            if (NodeImpl.getNodeImple().getPreVoteBallot().isGranted()) {
                ElectionService.checkToStartElection();
            }

        } catch (Exception e) {
            LOG.error("Handle preVote response error: {}",e.getMessage());
            //e.printStackTrace();
        }finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }
    }

    @Override
    public void handleElectionResponse(RpcRequests.RequestVoteResponse requestVoteResponse) {
        LOG.info("Handle vote response from: {} result: {}"
                ,requestVoteResponse.getPeerName(),requestVoteResponse.getGranted());
        NodeImpl.getNodeImple().getWriteLock().lock();
        try {
            if (requestVoteResponse.getGranted()) {
                NodeImpl.getNodeImple().getElectionBallot().grant(requestVoteResponse.getPeerName());
            }
            if (NodeImpl.getNodeImple().getElectionBallot().isGranted()) {
                LOG.info("Current node start to perform as leader,grant peer list {}"
                        ,NodeImpl.getNodeImple().getElectionBallot().getGranted()
                );
                NodeImpl.getNodeImple().startToPerformAsLeader();
            }

        } catch (Exception e) {
            LOG.error("Handle preVote response error: {}",e.getMessage());
        }finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }
    }


    @Override
    public Object call() throws Exception {
        return null;
    }
}

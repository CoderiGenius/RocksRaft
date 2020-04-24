package rpc;

import core.NodeImpl;
import entity.TimeOutChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.Utils;

import java.util.UUID;

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
    public RpcRequests.RequestPreVoteResponse handlePreVoteRequest(
            RpcRequests.RequestPreVoteRequest requestPreVoteRequest) {

          LOG.info("Receive preVoteRequest from {} ", requestPreVoteRequest.getPeerId()
            );
            long candidateTerm = requestPreVoteRequest.getLastLogTerm();
            long selfTerm = NodeImpl.getNodeImple().getLastLogTerm().get();
            long candidateLogIndex = requestPreVoteRequest.getLastLogIndex();
            long selfLogIndex = NodeImpl.getNodeImple().getLastLogIndex().longValue();
            RpcRequests.RequestPreVoteResponse.Builder builder = RpcRequests.RequestPreVoteResponse.newBuilder();
            builder.setTerm(selfTerm);
            builder.setPeerName(NodeImpl.getNodeImple().getNodeId().getPeerId().getPeerName());

            //check current node status
            if (!NodeImpl.getNodeImple().checkIfCurrentNodeCanVoteOthers()) {
                builder.setGranted(false);
                LOG.info("Current peer ignored the preVote request " +
                        "as the current node status is not for voting. status:{}", NodeImpl.getNodeImple().getNodeState());
                return builder.build();
            }

            //check if the term is valid and current leader is valid
            if (candidateTerm < selfTerm) {
                builder.setGranted(false);
                LOG.info("Current peer ignored the preVote request " +
                        "as the candidate's term {} is not as newer as the current {}", candidateTerm, selfTerm);
                return builder.build();
            } else if (NodeImpl.getNodeImple().isCurrentLeaderValid()) {
                builder.setGranted(false);
                LOG.info("Current peer ignored the preVote request " +
                        "as the current peer leader is still valid");

                return builder.build();
            }

            //check log entries
            if (candidateLogIndex < selfLogIndex) {
                builder.setGranted(false);
                LOG.info("Current peer ignored the preVote request " +
                        "as the candidate's LogIndex {} is not as newer as the current {}", candidateLogIndex, selfLogIndex);
                return builder.build();
            }
        //Every term can vote just once
//        if (NodeImpl.getNodeImple().getLastPreVoteTerm() == requestPreVoteRequest.getLastLogTerm()) {
//            builder.setGranted(false);
//            LOG.info("Current peer ignored the Prevote request " +
//                    "as the current node already Prevote others at term {}", requestPreVoteRequest.getLastLogTerm());
//            return builder.build();
//        }
            LOG.info("PreVote request from {} granted", requestPreVoteRequest.getPeerId());

        NodeImpl.getNodeImple().setLastPreVoteTerm(requestPreVoteRequest.getLastLogTerm());
        builder.setGranted(true);
            return builder.build();

    }

    @Override
    public RpcRequests.RequestVoteResponse handleVoteRequest(RpcRequests.RequestVoteRequest requestVoteRequest) {
        LOG.info("Receive VoteRequest from {}", requestVoteRequest.getPeerId());
        long candidateTerm = requestVoteRequest.getTerm();
        long selfTerm = NodeImpl.getNodeImple().getLastLogTerm().get();
        long candidateLogIndex = requestVoteRequest.getLastLogIndex();
        long selfLogIndex = NodeImpl.getNodeImple().getLastLogIndex().longValue();
        RpcRequests.RequestVoteResponse.Builder builder = RpcRequests.RequestVoteResponse.newBuilder();
        builder.setTerm(selfTerm);
        builder.setPeerName(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());


        //check current node status
        if (!NodeImpl.getNodeImple().checkIfCurrentNodeCanVoteOthers()) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the vote request from {}" +
                    "as the current node status is not for voting. status:{}",requestVoteRequest.getPeerId(),
                    NodeImpl.getNodeImple().getNodeState());
            return builder.build();
        }

        //check if the term is valid and current leader is valid
        if (candidateTerm < selfTerm) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the vote request " +
                    "as the candidate's term {} is not as newer as the current {}", candidateTerm, selfTerm);
            return builder.build();
        } else if (NodeImpl.getNodeImple().isCurrentLeaderValid()) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the vote request " +
                    "as the current peer leader is still valid");

            return builder.build();
        }

        //check log entries
        if (candidateLogIndex < selfLogIndex) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the vote request " +
                    "as the candidate's LogIndex {} is not as newer as the current {}", candidateLogIndex, selfLogIndex);
            return builder.build();
        }

        //Every term can vote just once
        if (NodeImpl.getNodeImple().getLastVoteTerm() == requestVoteRequest.getTerm()) {
            builder.setGranted(false);
            LOG.info("Current peer ignored the vote request " +
                    "as the current node already vote others at term {}", requestVoteRequest.getTerm());
            return builder.build();
        }
        NodeImpl.getNodeImple().setLastVoteTerm(requestVoteRequest.getTerm());
        builder.setGranted(true);
        return builder.build();
    }

    @Override
    public RpcRequests.AppendEntriesResponse handleApendEntriesRequest(RpcRequests.AppendEntriesRequest appendEntriesRequest) {
        RpcRequests.AppendEntriesResponse.Builder builder = RpcRequests.AppendEntriesResponse.newBuilder();

        try {
            NodeImpl.getNodeImple().getScheduledFuture().cancel(false);
            NodeImpl.getNodeImple().setChecker();


            //find out if it is null request and check if it is new leader request
            if (appendEntriesRequest.getData().isEmpty() &&
                    NodeImpl.getNodeImple().checkIfLeaderChanged(appendEntriesRequest.getPeerId())
            ) {
                //if add new leader failed
                if (!NodeImpl.getNodeImple().transformLeader(appendEntriesRequest)) {
                    LOG.error("add new Leader failed leaderID:{}",appendEntriesRequest.getPeerId());
                    return appendEntriesBuilder(builder, false).build();
                }
            } else {
                //normal appendEntry request

                //check term
                if (appendEntriesRequest.getTerm() != NodeImpl.getNodeImple().getLastLogTerm().get()) {
                    return appendEntriesBuilder(builder, false).build();
                }
                //check index
                if (appendEntriesRequest.getCommittedIndex() <= NodeImpl.getNodeImple().getLastLogIndex().get()) {
                    return appendEntriesBuilder(builder, false).build();
                }
                //check leader illegal

                //storage to log

                if (NodeImpl.getNodeImple().followerSetLogEvent(appendEntriesRequest)) {
                    return appendEntriesBuilder(builder, true).build();
                }
            }

            return appendEntriesBuilder(builder, false).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
return null;
    }

    private RpcRequests.AppendEntriesResponse.Builder appendEntriesBuilder(
            RpcRequests.AppendEntriesResponse.Builder builder,boolean result) {
        builder.setSuccess(result);
        builder.setLastLogIndex(NodeImpl.getNodeImple().getLastLogIndex().get());
        builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
        builder.setAddress(NodeImpl.getNodeImple().getCurrentEndPoint().getIp());
        builder.setPort(NodeImpl.getNodeImple().getCurrentEndPoint().getPort());
        builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
        return builder;
    }

    @Override
    public RpcRequests.AppendEntriesResponses handleApendEntriesRequests(RpcRequests.AppendEntriesRequests appendEntriesRequests) {
        RpcRequests.AppendEntriesResponses.Builder builder
                = RpcRequests.AppendEntriesResponses.newBuilder();

        boolean ret = true;
        for (int i = 0; i <appendEntriesRequests.getArgsCount() ; i++) {
            RpcRequests.AppendEntriesResponse appendEntriesResponse
                    = handleApendEntriesRequest(appendEntriesRequests.getArgs(i));
           builder.addArgs(appendEntriesResponse);
           if(!appendEntriesResponse.getSuccess()){
               ret = false;
           }

        }
        if (ret) {
            builder.setAppendEntriesStatus(RpcRequests.AppendEntriesStatus.APPROVED);
        }else {
            builder.setAppendEntriesStatus(RpcRequests.AppendEntriesStatus.FAILED);
        }
        return builder.build();

    }


    /**
     * Come from follower, leader invokes this method to handle follower stable event
     * @param notifyFollowerStableRequest
     * @return
     */
    @Override
    public RpcRequests.NotifyFollowerStableResponse handleFollowerStableRequest
    (RpcRequests.NotifyFollowerStableRequest notifyFollowerStableRequest) {
        RpcRequests.NotifyFollowerStableResponse.Builder builder = RpcRequests.NotifyFollowerStableResponse.newBuilder();

        if (NodeImpl.getNodeImple().handleFollowerStableEvent(notifyFollowerStableRequest)) {
            builder.setLastIndex(NodeImpl.getNodeImple().getLastLogIndex().get());
            builder.setSuccess(true);
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
            return builder.build();
        }else {
            builder.setLastIndex(NodeImpl.getNodeImple().getLastLogIndex().get());
            builder.setSuccess(false);
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
            return builder.build();
        }
    }


}

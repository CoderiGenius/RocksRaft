package rpc;

import com.google.protobuf.ZeroByteStringHelper;
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
            long candidateTerm = requestPreVoteRequest.getCommittedLogTerm();
            long selfTerm = NodeImpl.getNodeImple().getLastLogTerm().get();
            long candidateLogIndex = requestPreVoteRequest.getCommittedLogIndex();
            long selfLogIndex = NodeImpl.getNodeImple().getStableLogIndex().longValue();
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
            LOG.info("PreVote request from {} granted as the " +
                    "current term {} index {} and {} term {} and index {}"
                    , requestPreVoteRequest.getPeerId(),NodeImpl.getNodeImple().getLastLogTerm()
                    ,NodeImpl.getNodeImple().getLastLogTerm()
                    ,requestPreVoteRequest.getPeerId(),candidateTerm,candidateLogIndex);

        NodeImpl.getNodeImple().setLastPreVoteTerm(requestPreVoteRequest.getCommittedLogTerm());
        builder.setGranted(true);
            return builder.build();

    }

    @Override
    public RpcRequests.RequestVoteResponse handleVoteRequest(RpcRequests.RequestVoteRequest requestVoteRequest) {
        LOG.info("Receive VoteRequest from {}", requestVoteRequest.getPeerId());
        long candidateTerm = requestVoteRequest.getTerm();
        long selfTerm = NodeImpl.getNodeImple().getLastLogTerm().get();
        long candidateLogIndex = requestVoteRequest.getCommittedLogIndex();
        long selfLogIndex = NodeImpl.getNodeImple().getStableLogIndex().longValue();
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
    public RpcRequests.AppendEntriesResponse handleAppendEntriesRequest(RpcRequests.AppendEntriesRequest appendEntriesRequest) {
        RpcRequests.AppendEntriesResponse.Builder builder = RpcRequests.AppendEntriesResponse.newBuilder();

        LOG.debug("handleAppendEntriesRequest preLog isDataEmpty:{} index:{} term:{} peerID:{} addrsss:{}",
                appendEntriesRequest.getData().isEmpty(),appendEntriesRequest.getCommittedIndex(),appendEntriesRequest.getTerm()
                ,appendEntriesRequest.getPeerId()
                ,appendEntriesRequest.getAddress());

        if (NodeImpl.NodeState.leader.equals(NodeImpl.getNodeImple().getNodeState())) {
            LOG.debug("Receive appendEntries request from {} while in leader state index {} term {}"
                    ,appendEntriesRequest.getPeerId(),appendEntriesRequest.getCommittedIndex(),
                    appendEntriesRequest.getTerm());
        }
        //if it is a readIndex heartbeat then add the readIndex to the response for identify
        builder.setReadIndex(appendEntriesRequest.getReadIndex());
        try {
            NodeImpl.getNodeImple().getScheduledFuture().cancel(false);
            NodeImpl.getNodeImple().setLastReceiveHeartbeatTime(Utils.monotonicMs());
            NodeImpl.getNodeImple().setChecker();

            //check term
            if (appendEntriesRequest.getTerm() < NodeImpl.getNodeImple().getLastLogTerm().get()) {
                return appendEntriesBuilder(appendEntriesRequest,builder
                        ,"Term failure,target term is "
                                +NodeImpl.getNodeImple().getLastLogTerm().get()
                                +" but requester term is"+appendEntriesRequest.getTerm(), false).build();
            }
            //check index
            if (appendEntriesRequest.getCommittedIndex() < NodeImpl.getNodeImple().getStableLogIndex().get()) {
                return appendEntriesBuilder(appendEntriesRequest,builder
                        , "Index failure, target index is "
                        +NodeImpl.getNodeImple().getStableLogIndex().get()
                        +" but requester index is "+appendEntriesRequest.getCommittedIndex(),false).build();
            }

            //find out if it is null request and check if it is new leader request
            if (NodeImpl.getNodeImple().checkIfLeaderChanged(appendEntriesRequest.getPeerId()) &&
                            !appendEntriesRequest.getAddress().isEmpty()
            ) {
                //if add new leader failed
                if (!NodeImpl.getNodeImple().transformLeader(appendEntriesRequest)) {
                    LOG.error("add new Leader failed leaderID:{}",appendEntriesRequest.getPeerId());
                    return appendEntriesBuilder(appendEntriesRequest,builder,"Add leader failed", false).build();
                }
            }
            if(!appendEntriesRequest.getData().isEmpty()){
                //normal appendEntry request
                LOG.debug("Receive normal appendEntry request, index:{}",appendEntriesRequest.getCommittedIndex());

                //storage to log

                if (NodeImpl.getNodeImple().followerSetLogEvent(appendEntriesRequest)) {
                    return appendEntriesBuilder(appendEntriesRequest,builder,"", true).build();
                }
            }
            if (appendEntriesRequest.getData().isEmpty()) {
                //heart beat request
                return appendEntriesBuilder(appendEntriesRequest,builder,"", true).build();
            }

            return appendEntriesBuilder(appendEntriesRequest,builder, "unknow failure",false).build();

        } catch (Exception e) {
            e.printStackTrace();
        }
return null;
    }

    private RpcRequests.AppendEntriesResponse.Builder appendEntriesBuilder(
            RpcRequests.AppendEntriesRequest appendEntriesRequest,
            RpcRequests.AppendEntriesResponse.Builder builder,String reason,boolean result) {
        builder.setSuccess(result);

        builder.setLastLogIndex(appendEntriesRequest.getCommittedIndex());
        builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
        if (!"".equals(reason)) {
            LOG.error("appendEntries failure {}",reason);
        }
        builder.setReason(reason);
        builder.setAddress(NodeImpl.getNodeImple().getCurrentEndPoint().getIp());
        builder.setPort(NodeImpl.getNodeImple().getCurrentEndPoint().getPort());
        builder.setPeerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
        return builder;
    }

    @Override
    public RpcRequests.AppendEntriesResponses handleApendEntriesRequests
            (RpcRequests.AppendEntriesRequests appendEntriesRequests) {
        RpcRequests.AppendEntriesResponses.Builder builder
                = RpcRequests.AppendEntriesResponses.newBuilder();

        boolean ret = true;
        for (int i = 0; i <appendEntriesRequests.getArgsCount() ; i++) {
            LOG.warn("handleApendEntriesRequests :{}",appendEntriesRequests.getArgs(i).getCommittedIndex());
            RpcRequests.AppendEntriesResponse appendEntriesResponse
                    = handleAppendEntriesRequest(appendEntriesRequests.getArgs(i));
            LOG.warn("Check logEntry data:{} at:{}",
                    ZeroByteStringHelper.byteStringToString(appendEntriesRequests.getArgs(i).getData())
                    ,appendEntriesRequests.getArgs(i).getCommittedIndex());
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
     * Come from follower, leader run this method to handle follower stable event
     * @param notifyFollowerStableRequest
     * @return
     */
    @Override
    public RpcRequests.NotifyFollowerStableResponse handleFollowerStableRequest
    (RpcRequests.NotifyFollowerStableRequest notifyFollowerStableRequest) {
        LOG.debug("Receive follower stable request from follower:{} at index {} length {}"
                ,notifyFollowerStableRequest.getPeerId(),notifyFollowerStableRequest.getLastIndex(),
                notifyFollowerStableRequest.getLastIndex()-notifyFollowerStableRequest.getFirstIndex());
        RpcRequests.NotifyFollowerStableResponse.Builder builder = RpcRequests.NotifyFollowerStableResponse.newBuilder();

        if (NodeImpl.getNodeImple().handleFollowerStableEvent(notifyFollowerStableRequest)) {
            builder.setLastIndex(NodeImpl.getNodeImple().getStableLogIndex().get());
            builder.setSuccess(true);
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
            return builder.build();
        }else {
            builder.setLastIndex(NodeImpl.getNodeImple().getStableLogIndex().get());
            builder.setSuccess(false);
            builder.setTerm(NodeImpl.getNodeImple().getLastLogTerm().get());
            return builder.build();
        }
    }


    /**
     * invoke by leader, towards the follower ,notify them to apply the given index into statemachine
     * @param request
     * @return
     */
    @Override
    public RpcRequests.NotifyFollowerToApplyResponse handleToApplyRequest(RpcRequests.NotifyFollowerToApplyRequest request) {

        RpcRequests.NotifyFollowerToApplyResponse.Builder builder
                = RpcRequests.NotifyFollowerToApplyResponse.newBuilder();
        if(NodeImpl.getNodeImple().getStableLogIndex().get()>request.getLastIndex()){
            builder.setSuccess(true);
        }else {
            boolean b = NodeImpl.getNodeImple().getFsmCaller().onCommitted(request.getLastIndex());
            if (b) {
                builder.setSuccess(true);
                builder.setLastIndex(request.getLastIndex());
            }else {
                builder.setSuccess(false);
                builder.setLastIndex(request.getLastIndex());
            }
        }


        builder.setFollowerId(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
        return builder.build();
    }

    @Override
    public RpcRequests.AppendEntriesResponse handleReadHeartbeatrequest(RpcRequests.AppendEntriesRequest appendEntriesRequest) {
       return handleAppendEntriesRequest(appendEntriesRequest);
    }


}

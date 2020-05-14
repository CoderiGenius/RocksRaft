package rpc;

/**
 * Created by 周思成 on  2020/3/13 11:51
 */

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import com.google.protobuf.Message;
import core.NodeImpl;
import entity.Closure;
import entity.Node;
import entity.TimeOutChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ElectionService;
import service.ElectionServiceImpl;
import utils.Utils;

import java.util.List;


/**
 * @author Mike
 */
public class RpcResponseClosure<T>  implements SofaResponseCallback<T> {

    public static final Logger LOG = LoggerFactory.getLogger(RpcResponseClosure.class);
    ElectionService electionService = new ElectionServiceImpl();
    @Override
    public void onAppResponse(Object o, String s, RequestBase requestBase) {

        LOG.debug("Recvieve response:requestBase: {} requestString: {}",requestBase.toString(),s);
        switch (requestBase.getMethodName()) {
            case "handleApendEntriesRequest":
                RpcRequests.AppendEntriesResponse appendEntriesResponse =
                        (RpcRequests.AppendEntriesResponse)o;
                NodeImpl.getNodeImple().handleAppendEntriesResponse(appendEntriesResponse);
            case "handlePreVoteRequest":
                RpcRequests.RequestPreVoteResponse requestPreVoteResponse =
                        (RpcRequests.RequestPreVoteResponse)o;
                electionService.handlePrevoteResponse(requestPreVoteResponse);
                return;
            case "handleVoteRequest":
                RpcRequests.RequestVoteResponse requestVoteResponse =
                        (RpcRequests.RequestVoteResponse)o;
                electionService.handleElectionResponse(requestVoteResponse);
                return;
            case "handleToApplyRequest":
                RpcRequests.NotifyFollowerToApplyResponse response =
                        (RpcRequests.NotifyFollowerToApplyResponse)o;
                if(!response.getSuccess()){
                    LOG.error("***critical error*** FSM StateMachine apply failed follower{}"
                            ,response.getFollowerId());
                }else {
                    NodeImpl.getNodeImple().handleToApplyResponse(response);
                }
            case "handleReadHeartbeatrequest":
                RpcRequests.AppendEntriesResponse appendEntriesResponse1
                        = (RpcRequests.AppendEntriesResponse)o;
                NodeImpl.getNodeImple().handleReadHeartbeatRequestClosure(appendEntriesResponse1);

            default:
                LOG.error("RPC Request closure mismatched, requestBase: {} requestString: {}"
                        ,requestBase.toString(),s);
        }
    }

    @Override
    public void onAppException(Throwable throwable, String s, RequestBase requestBase) {

    }

    @Override
    public void onSofaException(SofaRpcException e, String s, RequestBase requestBase) {

    }
}

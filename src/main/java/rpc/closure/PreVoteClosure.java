package rpc.closure;

import com.alipay.sofa.rpc.core.request.RequestBase;
import entity.PeerId;
import rpc.RpcRequests;
import rpc.RpcResponseClosure;

/**
 * Created by 周思成 on  2020/3/30 23:25
 */

@Deprecated
public class PreVoteClosure extends RpcResponseClosure<RpcRequests.RequestPreVoteRequest> {

    final long         startMs;
    final PeerId peer;
    final long         term;
    RpcRequests.RequestPreVoteRequest requestPreVoteRequest;

    public PreVoteClosure(long startMs, PeerId peer, long term, RpcRequests.RequestPreVoteRequest requestPreVoteRequest) {
        this.startMs = startMs;
        this.peer = peer;
        this.term = term;
        this.requestPreVoteRequest = requestPreVoteRequest;

    }

    public long getStartMs() {
        return startMs;
    }

    public PeerId getPeer() {
        return peer;
    }

    public long getTerm() {
        return term;
    }

    public RpcRequests.RequestPreVoteRequest getRequestVoteRequest() {
        return requestPreVoteRequest;
    }

    public void setRequestVoteRequest(RpcRequests.RequestPreVoteRequest requestVoteRequest) {
        this.requestPreVoteRequest = requestVoteRequest;
    }



    @Override
    public void onAppResponse(Object o, String s, RequestBase requestBase) {


    }
}

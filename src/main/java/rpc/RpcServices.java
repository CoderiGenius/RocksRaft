package rpc;

import rpc.closure.PreVoteClosure;

/**
 * Created by 周思成 on  2020/3/24 12:47
 */

public interface RpcServices {


    public RpcRequests sendRpcRequest(RpcRequests rpcRequests);

    public RpcRequests.RequestPreVoteResponse handlePreVoteRequest(
            RpcRequests.RequestPreVoteRequest preVoteRequest);

    public RpcRequests.RequestVoteResponse handleVoteRequest(
            RpcRequests.RequestVoteRequest requestVoteRequest);
}
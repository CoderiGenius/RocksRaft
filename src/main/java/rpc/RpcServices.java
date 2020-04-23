package rpc;



/**
 * Created by 周思成 on  2020/3/24 12:47
 */

public interface RpcServices {


    public RpcRequests sendRpcRequest(RpcRequests rpcRequests);

    public RpcRequests.RequestPreVoteResponse handlePreVoteRequest(
            RpcRequests.RequestPreVoteRequest preVoteRequest);

    public RpcRequests.RequestVoteResponse handleVoteRequest(
            RpcRequests.RequestVoteRequest requestVoteRequest);

    public RpcRequests.AppendEntriesResponse handleApendEntriesRequest(
            RpcRequests.AppendEntriesRequest appendEntriesRequest);

    public RpcRequests.AppendEntriesResponses handleApendEntriesRequests(
            RpcRequests.AppendEntriesRequests appendEntriesRequests);

    public RpcRequests.NotifyFollowerStableResponse handleFollowerStableRequest(
            RpcRequests.NotifyFollowerStableRequest notifyFollowerStableRequest
    );
}

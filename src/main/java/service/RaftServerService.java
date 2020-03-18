package service;

import com.google.protobuf.Message;
import rpc.RpcRequestClosure;
import rpc.RpcRequests;
import rpc.RpcRequests;
import rpc.RpcResponseClosure;

/**
 * Created by 周思成 on  2020/3/12 14:33
 * @author Mike
 */

public interface RaftServerService {


    /**
     * Handle pre-vote request.
     *
     * @param request   data of the pre vote
     * @return the response message
     */
    Message handlePreVoteRequest(RpcRequests.RequestVoteRequest request);

    /**
     * Handle request-vote request.
     *
     * @param request   data of the vote
     * @return the response message
     */
    Message handleRequestVoteRequest(RpcRequests.RequestVoteRequest request);

    /**
     * Handle append-entries request, return response message or
     * called done.run() with response.
     *
     * @param request   data of the entries to append
     * @param done      callback
     * @return the response message
     */
    Message handleAppendEntriesRequest(RpcRequests.AppendEntriesRequest request, RpcRequestClosure done);



    /**
     * Handle time-out-now request, return response message or
     * called done.run() with response.
     *
     * @param request   data of the timeout now request
     * @param done      callback
     * @return the response message
     */
    Message handleTimeoutNowRequest(RpcRequests.TimeoutNowRequest request, RpcRequestClosure done);

    /**
     * Handle read-index request, call the RPC.proto closure with response.
     *
     * @param request   data of the readIndex read
     * @param done      callback
     */
    void handleReadIndexRequest(RpcRequests.ReadIndexRequest request, RpcResponseClosure<RpcRequests.ReadIndexResponse> done);
}

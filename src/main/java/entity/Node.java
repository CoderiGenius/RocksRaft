package entity;

import core.ReplicatorStateListener;
import rpc.RpcRequests;

import java.util.List;

/**
 * raft节点
 * Created by 周思成 on  2020/3/10 14:47
 */


public interface Node  {

    /**
     * Get the leader peer id for redirect, null if absent.
     */
    NodeId getLeaderId();

    /**
     * Get current node id.
     */
    NodeId getNodeId();

    /**
     * Start To Perform As the Leader
     */
    void startToPerformAsLeader();

//    RpcRequests.AppendEntriesResponse nullAppendEntriesHandler(RpcRequests.AppendEntriesRequest appendEntriesRequest);

    List<ReplicatorStateListener> getReplicatorStatueListeners();

    boolean transformLeader(RpcRequests.AppendEntriesRequest request);

    void apply(Task task);

    boolean init();
}

package entity;

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

}

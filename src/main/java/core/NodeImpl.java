package core;

import entity.Heartbeat;
import entity.Node;
import entity.NodeId;
import entity.PeerId;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by 周思成 on  2020/3/25 14:09
 */

public class NodeImpl implements Node {
    /**
     * 表示节点状态的enum
     * leader follower领导者跟随者
     * candidate 正式选举
     * preCandidate 预选举
     */
    public enum NodeState{leader,follwoer,candidate,preCandidate}


    private NodeState nodeState;
    private static NodeImpl nodeImple = new NodeImpl();

    private NodeImpl(){}

    private NodeId nodeId;

    private  Heartbeat heartbeat;


    private Long term;
    /**
     * 上一次收到心跳包的时间
     */
    private AtomicLong lastReceiveHeartbeatTime;



    @Override
    public PeerId getLeaderId() {
        return null;
    }

    @Override
    public NodeId getNodeId() {
        return null;
    }


    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    public void setNodeId(NodeId nodeId) {
        this.nodeId = nodeId;
    }

    public AtomicLong getLastReceiveHeartbeatTime() {
        return lastReceiveHeartbeatTime;
    }

    public void setLastReceiveHeartbeatTime(AtomicLong lastReceiveHeartbeatTime) {
        this.lastReceiveHeartbeatTime = lastReceiveHeartbeatTime;
    }

    public static NodeImpl getNodeImple() {
        return nodeImple;
    }

    private static void setNodeImple(NodeImpl nodeImple) {
        NodeImpl.nodeImple = nodeImple;
    }

    public Heartbeat getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(Heartbeat heartbeat) {
        this.heartbeat = heartbeat;
    }

    public NodeState getNodeState() {
        return nodeState;
    }

    public void setNodeState(NodeState nodeState) {
        this.nodeState = nodeState;
    }

    public boolean checkNodeStateCandidate(){
        return NodeState.candidate.equals(getNodeState());
    }

    public boolean checkNodeStatePreCandidate(){
        return NodeState.preCandidate.equals(getNodeState());
    }
}

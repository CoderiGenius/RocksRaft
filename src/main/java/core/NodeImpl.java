package core;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import entity.*;
import rpc.RpcServices;
import utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
    public enum NodeState {leader, follwoer, candidate, preCandidate,NODE_STATE}
    private static final NodeImpl NODE_IMPLE = new NodeImpl();

    private NodeImpl() {
    }

    private final Lock reentrantLock = new ReentrantLock(true);
    private final ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
    protected final Lock     writeLock = this.readWriteLock.writeLock();
    protected final Lock    readLock  = this.readWriteLock.readLock();
    private List<PeerId> peerIdList = new CopyOnWriteArrayList<>();
    private Map<Endpoint,RpcServices> rpcServices = new ConcurrentHashMap<>();
    private NodeState nodeState;
    private AtomicLong lastLogTerm = new AtomicLong(0);
    private AtomicLong lastLogIndex = new AtomicLong(0);

    /**
     * Current node entity, including peedId inside
     */
    private NodeId nodeId;
    private NodeId leaderId;

    private Ballot ballot = new Ballot(peerIdList);

    private Heartbeat heartbeat;


    private Long term;
    /**
     * 上一次收到心跳包的时间
     */
    private AtomicLong lastReceiveHeartbeatTime;


    @Override
    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }


    public boolean checkIfCurrentNodeCanVoteOthers(){
        //only leader, follower, preCandidate, NODE_STATE can vote others
        if (NodeState.preCandidate.equals(getNodeState())
                || NodeState.follwoer.equals(getNodeState())
                || NodeState.leader.equals(getNodeState())
                || NodeState.NODE_STATE.equals(getNodeState())) {
            return true;
        }else {
            return false;
        }
    }

    public boolean checkIfCurrentNodeCanStartPreVote(){
        //only  follower,None_state  can vote others
        if (NodeState.NODE_STATE.equals(getNodeState())
                || NodeState.follwoer.equals(getNodeState())
               ) {
            return true;
        }else {
            return false;
        }
    }

    public Long getTerm() {
        return term;
    }

    public void setTerm(Long term) {
        this.term = term;
    }

    public Map<Endpoint, RpcServices> getRpcServices() {
        return rpcServices;
    }

    public void setRpcServices(Map<Endpoint, RpcServices> rpcServices) {
        this.rpcServices = rpcServices;
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
        return NODE_IMPLE;
    }

    public AtomicLong getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(AtomicLong lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }

    public AtomicLong getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(AtomicLong lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public List<PeerId> getPeerIdList() {
        return peerIdList;
    }


    public void setPeerIdList(List<PeerId> peerIdList) {
        this.peerIdList = peerIdList;
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

    public boolean checkNodeStateCandidate() {
        return NodeState.candidate.equals(getNodeState());
    }

    public boolean checkNodeStatePreCandidate() {
        return NodeState.preCandidate.equals(getNodeState());
    }

    public boolean isCurrentLeaderValid() {
        return Utils.monotonicMs() - this.lastReceiveHeartbeatTime.get() < NodeOptions.getNodeOptions().getMaxHeartBeatTime();
    }
}

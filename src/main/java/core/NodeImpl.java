package core;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import config.ReplicatorOptions;
import entity.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcRequests;
import rpc.RpcServices;
import rpc.RpcServicesImpl;
import rpc.TaskRpcServices;
import utils.Requires;
import utils.TimerManager;
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

    public static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);
    /**
     * 表示节点状态的enum
     * leader follower领导者跟随者
     * candidate 正式选举
     * preCandidate 预选举
     */
    public enum NodeState {leader, follwoer, candidate, preCandidate,NODE_STATE}
    private static  final NodeImpl NODE_IMPLE = new NodeImpl();

    private NodeImpl() {
    }
    /** ReplicatorStateListeners */
    private final CopyOnWriteArrayList<ReplicatorStateListener> replicatorStateListeners = new CopyOnWriteArrayList<>();

    private final Lock reentrantLock = new ReentrantLock(true);
    private final ReadWriteLock  readWriteLock = new ReentrantReadWriteLock();
    protected final Lock     writeLock = this.readWriteLock.writeLock();
    protected final Lock    readLock  = this.readWriteLock.readLock();
    private List<PeerId> peerIdList = new CopyOnWriteArrayList<>();
    private Map<Endpoint, RpcServices> rpcServicesMap = new ConcurrentHashMap<>();
    private Map<Endpoint, TaskRpcServices> taskRpcServices = new ConcurrentHashMap<>();
    private Map<Endpoint,Replicator> replicatorMap = new ConcurrentHashMap<>();
    private NodeState nodeState;
    private AtomicLong lastLogTerm = new AtomicLong(0);
    private AtomicLong lastLogIndex = new AtomicLong(0);
    private PeerId currentLeaderId;
    private Options options;

    /**
     * Current node entity, including peedId inside
     */
    private NodeId nodeId;
    private NodeId leaderId;

    private Ballot preVoteBallot;
    private Ballot electionBallot;
    private Heartbeat heartbeat;
    private LogManager logManager;


    /**
     * 上一次收到心跳包的时间
     */
    private AtomicLong lastReceiveHeartbeatTime;


    public void setLeaderId(NodeId leaderId) {
        this.leaderId = leaderId;
    }

    public Map<Endpoint, TaskRpcServices> getTaskRpcServices() {
        return taskRpcServices;
    }

    public void setTaskRpcServices(Map<Endpoint, TaskRpcServices> taskRpcServices) {
        this.taskRpcServices = taskRpcServices;
    }

    @Override
    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public NodeId getNodeId() {
        return nodeId;
    }

    @Override
    public void startToPerformAsLeader() {
        if (NodeState.leader.equals(NodeImpl.getNodeImple().getNodeState())) {
            return;
        }
        NodeImpl.getNodeImple().getWriteLock().lock();
        try {
            NodeImpl.getNodeImple().setNodeState(NodeState.leader);
            //generate replicator for all followers
            for (PeerId peerId : getPeerIdList()) {

                ReplicatorOptions replicatorOptions = new ReplicatorOptions(
                        getOptions().getCurrentNodeOptions().getMaxHeartBeatTime(),
                        getOptions().getCurrentNodeOptions().getElectionTimeOut()
                        ,getNodeId().getGroupId(),getNodeId().getPeerId(),getLogManager()
                        ,new Ballot(getPeerIdList()),getNodeImple(),getLastLogTerm().longValue()
                        ,new TimerManager(),ReplicatorType.Follower);
                Replicator replicator = new Replicator(replicatorOptions,rpcServicesMap.get(peerId.getEndpoint()));

                replicatorMap.put(peerId.getEndpoint(),replicator);

            }

        } catch (Exception e) {

        }finally {
            NodeImpl.getNodeImple().getWriteLock().unlock();
        }

    }



//    @Override
//    public RpcRequests.AppendEntriesResponse nullAppendEntriesHandler(RpcRequests.AppendEntriesRequest appendEntriesRequest) {
//
//        getWriteLock().lock();
//        RpcRequests.AppendEntriesResponse.Builder builder = RpcRequests.AppendEntriesResponse.newBuilder();
//        try {
//            //check term and if leader is changed
//            if (appendEntriesRequest.getTerm() > getLastLogTerm().longValue()
//                    && getLeaderId().getPeerId().getPeerName().equals(appendEntriesRequest.getPeerId())) {
//
//
//            }
//        } catch (Exception e) {
//            LOG.error("handling null appendEntries request error:"+e.getMessage());
//        }finally {
//            getWriteLock().unlock();
//        }
//        return builder.build();
//    }

    @Override
    public List<ReplicatorStateListener> getReplicatorStatueListeners() {
        return this.replicatorStateListeners;
    }

    @Override
    public boolean transformLeader(RpcRequests.AppendEntriesRequest request) {
        LOG.info("Start to transform Leader from {} to {}"
                ,getLeaderId().getPeerId().getPeerName(),request.getPeerId());
        getWriteLock().lock();
        try {
            //check if leader is valid
            if ( ! checkNodeAheadOfCurrent(request.getTerm(), request.getCommittedIndex())) {
               LOG.error("leader is invalid with term {} index {} while current term {} index {}"
                       ,request.getTerm(), request.getCommittedIndex()
                       ,getLastLogTerm(),getLastLogIndex());
                return false;
            }

            setNodeState(NodeState.follwoer);
            PeerId peerId = new PeerId(request.getPeerId(),request.getPeerId()
                    ,request.getAddress(),request.getPort(),request.getTaskPort());
            NodeId nodeId = new NodeId(request.getGroupId(),peerId);
            setLeaderId(nodeId);

            return true;
        } catch (Exception e) {
            LOG.error("Transform leader error:{}",e.getMessage());
        }finally {
            getWriteLock().unlock();
        }
        return false;
    }

    @Override
    public void apply(Task task) {
        Requires.requireNonNull(task, "Null task");
        LOG.info("Applying task");

        if (NodeState.follwoer.equals(getNodeState())) {
            LOG.info("Current node is in follower state, leader is {} forwarding request……"
                    ,getLeaderId().getPeerId().getPeerName());
            getTaskRpcServices().get(getLeaderId().getPeerId().getEndpoint()).apply(task);
        } else if (!NodeState.leader.equals(getNodeState())) {
            LOG.info("Current node is not in valid state {}",getNodeState());
            Utils.runClosureInThread(task.getDone()
                    , new Status(RaftError.ENODESHUTDOWN, "Current node is not in valid state {}",getNodeState()));


        }



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

    private boolean checkNodeAheadOfCurrent(long term, long index) {
        return term > getLastLogTerm().longValue() && index > getLastLogIndex().longValue();
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public boolean checkIfCurrentNodeCanStartPreVote(){
        //only  follower,None_state  can start PreVote
        if (NodeState.NODE_STATE.equals(getNodeState())
                || NodeState.follwoer.equals(getNodeState())
               ) {
            return true;
        }else {
            return false;
        }
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public boolean checkIfCurrentNodeCanStartElection(){
        //only  PreVote can start PreVote
        if (NodeState.preCandidate.equals(NodeImpl.getNodeImple().getNodeState())
        ) {
            return true;
        }else {
            return false;
        }
    }

    public Ballot getPreVoteBallot() {
        return preVoteBallot;
    }

    public void setPreVoteBallot(Ballot preVoteBallot) {
        this.preVoteBallot = preVoteBallot;
    }





    public Map<Endpoint, RpcServices> getRpcServicesMap() {
        return rpcServicesMap;
    }

    public Ballot getElectionBallot() {
        return electionBallot;
    }

    public void setElectionBallot(Ballot electionBallot) {
        this.electionBallot = electionBallot;
    }

    public void setRpcServicesMap(Map<Endpoint, RpcServices> rpcServicesMap) {
        this.rpcServicesMap = rpcServicesMap;
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

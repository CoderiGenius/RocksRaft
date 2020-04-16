package core;

import config.ReplicatorOptions;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.RpcRequests;
import rpc.RpcResponseClosure;
import utils.CatchUpClosure;
import utils.Requires;
import utils.ThreadId;
import utils.ThreadPoolUtil;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by 周思成 on  2020/4/14 16:30
 */

public class ReplicatorGroupImpl implements ReplicatorGroup {

    private static final Logger LOG                = LoggerFactory
            .getLogger(ReplicatorGroupImpl.class);
    // <peerId, replicatorId>
    //private final ConcurrentMap<PeerId, ThreadId> replicatorMap      = new ConcurrentHashMap<>();
    private final ConcurrentMap<PeerId, Replicator> replicatorMap      = new ConcurrentHashMap<>();
    /** common replicator options */
    private ReplicatorOptions commonOptions;
    private int                                   dynamicTimeoutMs   = -1;
    private int                                   electionTimeoutMs  = -1;

    private final Map<PeerId, ReplicatorType> failureReplicators = new ConcurrentHashMap<>();

    private final ThreadPoolExecutor replicatorGroupThreadPool = new ThreadPoolExecutor(
            NodeImpl.getNodeImple().getOptions().getOtherNodes().length,
            NodeImpl.getNodeImple().getOptions().getOtherNodes().length+1,
            10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
            new ThreadFactoryImpl(),new RejectedExecutionHandlerImpl());
    private class ThreadFactoryImpl implements ThreadFactory{

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"Replicator-"+commonOptions.getPeerId().getPeerName());

        }
    }
    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler{

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOG.error("ReplicatorGroup ThreadPool error:rejected");
        }
    }
    @Override
    public boolean init(NodeId nodeId, ReplicatorGroupOptions opts) {
        this.dynamicTimeoutMs = opts.getHeartbeatTimeoutMs();
        this.electionTimeoutMs = opts.getElectionTimeoutMs();

        this.commonOptions = new ReplicatorOptions();
        this.commonOptions.setDynamicHeartBeatTimeoutMs(this.dynamicTimeoutMs);
        this.commonOptions.setElectionTimeoutMs(this.electionTimeoutMs);
        //this.commonOptions.setRaftRpcService(opts.getRaftRpcClientService());
        this.commonOptions.setLogManager(opts.getLogManager());
        this.commonOptions.setBallotBox(opts.getBallotBox());
        this.commonOptions.setNode(opts.getNode());
        this.commonOptions.setTerm(0);
        this.commonOptions.setGroupId(nodeId.getGroupId());
        this.commonOptions.setServerId(nodeId.getPeerId());

        this.commonOptions.setTimerManager(opts.getTimerManager());
        return true;
    }

    @Override
    public boolean addReplicator(PeerId peer,Replicator replicator) {
        Requires.requireNonNull(peer,"peer");
        Requires.requireNonNull(replicator,"replicator");
        replicatorMap.put(peer,replicator);
        replicator.start();
        LOG.info("add replicator: {}",peer);
        return true;
    }

    @Override
    public boolean addReplicator(PeerId peer, ReplicatorType replicatorType) {
        return false;
    }

    @Override
    public void sendHeartbeat(Replicator replicator) {
        Requires.requireNonNull(replicator,"replicator");
        replicator.sendEmptyEntries(true);
    }

    @Override
    public void sendHeartbeatToAll() {
        for (Map.Entry<PeerId, Replicator> peerIdReplicatorEntry : replicatorMap.entrySet()) {
            PeerId peerId = (PeerId) peerIdReplicatorEntry;
            sendHeartbeat(replicatorMap.get(peerId));
        }
    }

    @Override
    public Replicator getReplicator(PeerId peer) {
        return replicatorMap.get(peer);
    }

    @Override
    public void checkReplicator(PeerId peer, boolean lockNode) {

    }

    @Override
    public void clearFailureReplicators() {

    }

    @Override
    public boolean waitCaughtUp(PeerId peer, long maxMargin, long dueTime, CatchUpClosure done) {
        return false;
    }

    @Override
    public long getLastRpcSendTimestamp(PeerId peer) {
        return 0;
    }

    @Override
    public boolean stopAll() {
        return false;
    }

    @Override
    public boolean stopReplicator(PeerId peer) {
        return false;
    }

    @Override
    public boolean resetTerm(long newTerm) {
        return false;
    }

    @Override
    public boolean resetHeartbeatInterval(int newIntervalMs) {
        return false;
    }

    @Override
    public boolean resetElectionTimeoutInterval(int newIntervalMs) {
        return false;
    }

    @Override
    public boolean contains(PeerId peer) {
        return false;
    }

    @Override
    public boolean transferLeadershipTo(PeerId peer, long logIndex) {
        return false;
    }

    @Override
    public boolean stopTransferLeadership(PeerId peer) {
        return false;
    }

    @Override
    public ThreadId stopAllAndFindTheNextCandidate(ConfigurationEntry conf) {
        return null;
    }

    @Override
    public PeerId findTheNextCandidate(ConfigurationEntry conf) {
        return null;
    }

    @Override
    public List<ThreadId> listReplicators() {
        return null;
    }


    public ThreadPoolExecutor getReplicatorGroupThreadPool() {
        return replicatorGroupThreadPool;
    }
}

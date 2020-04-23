package core;

import com.lmax.disruptor.EventTranslator;
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
    private final List<Replicator> replicatorList      = new CopyOnWriteArrayList<>();
    /** common replicator options */
    private ReplicatorOptions commonOptions;
    private long                                   dynamicTimeoutMs   = -1;
    private long                                   electionTimeoutMs  = -1;

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
        this.commonOptions.setDynamicHeartBeatTimeoutMs(opts.getHeartbeatTimeoutMs());
        this.commonOptions.setElectionTimeoutMs(opts.getElectionTimeoutMs());
        //this.commonOptions.setRaftRpcService(opts.getRaftRpcClientService());
        this.commonOptions.setLogManager(opts.getLogManager());
        //this.commonOptions.setBallotBox(opts.getBallotBox());
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
        replicatorList.add(replicator);
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
        for (Replicator r :
                replicatorList) {
            sendHeartbeat(r);
        }
    }

    @Override
    public Replicator getReplicator(PeerId peer) {
        return replicatorMap.get(peer);
    }

    @Override
    public void sendAppendEntriesToAllReplicator(List<LogEntry> logEntries) {

        for (Replicator r :
                replicatorList) {
            for (LogEntry l :
                    logEntries) {
            final EventTranslator<LogEntry> translator = (event, sequence) -> { event = l; };
                r.getDisruptor().publishEvent(translator);
            }

        }
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

    @Override
    public boolean sendInflight(String address,int port,long currentIndexOfFollower){
        for (Replicator r :
                replicatorList) {
            if (r.getEndpoint().getIp().equals(address) && r.getEndpoint().getPort() == port) {
                r.handleProbeOrFollowerDisOrderResponse(currentIndexOfFollower);
                return true;
            }
        }
        return false;
    }

    public ThreadPoolExecutor getReplicatorGroupThreadPool() {
        return replicatorGroupThreadPool;
    }

    public ConcurrentMap<PeerId, Replicator> getReplicatorMap() {
        return replicatorMap;
    }

    public List<Replicator> getReplicatorList() {
        return replicatorList;
    }

    public ReplicatorOptions getCommonOptions() {
        return commonOptions;
    }

    public void setCommonOptions(ReplicatorOptions commonOptions) {
        this.commonOptions = commonOptions;
    }

    public long getDynamicTimeoutMs() {
        return dynamicTimeoutMs;
    }

    public void setDynamicTimeoutMs(long dynamicTimeoutMs) {
        this.dynamicTimeoutMs = dynamicTimeoutMs;
    }

    public long getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(long electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public Map<PeerId, ReplicatorType> getFailureReplicators() {
        return failureReplicators;
    }
}


package config;

import core.NodeImpl;
import entity.Ballot;
import entity.LogManager;
import entity.PeerId;
import entity.ReplicatorType;
import utils.TimerManager;

/**
 * Created by 周思成 on  2020/4/6 7:32
 */

public class ReplicatorOptions {
    private long               dynamicHeartBeatTimeoutMs;
    private long               electionTimeoutMs;
    private String            groupId;
    private PeerId serverId;
    private PeerId            peerId;
    private LogManager logManager;
    private Ballot ballotBox;
    private NodeImpl node;
    private long              term;
    private TimerManager timerManager;
    private ReplicatorType replicatorType;

    public ReplicatorOptions(long dynamicHeartBeatTimeoutMs, long electionTimeoutMs
            , String groupId,  PeerId peerId, LogManager logManager
            , Ballot ballotBox, NodeImpl node, long term, TimerManager timerManager
            , ReplicatorType replicatorType) {
        this.dynamicHeartBeatTimeoutMs = dynamicHeartBeatTimeoutMs;
        this.electionTimeoutMs = electionTimeoutMs;
        this.groupId = groupId;

        this.peerId = peerId;
        this.logManager = logManager;
        this.ballotBox = ballotBox;
        this.node = node;
        this.term = term;
        this.timerManager = timerManager;
        this.replicatorType = replicatorType;
    }

    public long getDynamicHeartBeatTimeoutMs() {
        return dynamicHeartBeatTimeoutMs;
    }

    public void setDynamicHeartBeatTimeoutMs(long dynamicHeartBeatTimeoutMs) {
        this.dynamicHeartBeatTimeoutMs = dynamicHeartBeatTimeoutMs;
    }

    public long getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public void setElectionTimeoutMs(long electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public PeerId getServerId() {
        return serverId;
    }

    public void setServerId(PeerId serverId) {
        this.serverId = serverId;
    }

    public PeerId getPeerId() {
        return peerId;
    }

    public void setPeerId(PeerId peerId) {
        this.peerId = peerId;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public Ballot getBallotBox() {
        return ballotBox;
    }

    public void setBallotBox(Ballot ballotBox) {
        this.ballotBox = ballotBox;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    public ReplicatorType getReplicatorType() {
        return replicatorType;
    }

    public void setReplicatorType(ReplicatorType replicatorType) {
        this.replicatorType = replicatorType;
    }
}

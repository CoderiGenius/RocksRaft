package entity;

import core.NodeImpl;
import utils.TimerManager;

/**
 * Created by 周思成 on  2020/4/14 15:33
 */

public class ReplicatorGroupOptions {

    private long               heartbeatTimeoutMs;
    private long               electionTimeoutMs;
    private LogManager        logManager;
    //private BallotBox         ballotBox;
    private NodeImpl node;

    public ReplicatorGroupOptions(long heartbeatTimeoutMs,
                                  long electionTimeoutMs,
                                  LogManager logManager
                                  ) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
        this.electionTimeoutMs = electionTimeoutMs;
        this.logManager = logManager;


    }

    private TimerManager timerManager;

    public TimerManager getTimerManager() {
        return this.timerManager;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }




    public void setHeartbeatTimeoutMs(int heartbeatTimeoutMs) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }


    public void setElectionTimeoutMs(int electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }

    public LogManager getLogManager() {
        return this.logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public long getHeartbeatTimeoutMs() {
        return heartbeatTimeoutMs;
    }

    public long getElectionTimeoutMs() {
        return electionTimeoutMs;
    }

    public NodeImpl getNode() {
        return this.node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }


    public void setHeartbeatTimeoutMs(long heartbeatTimeoutMs) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }

    @Override
    public String toString() {
        return "ReplicatorGroupOptions{" +
                "heartbeatTimeoutMs=" + heartbeatTimeoutMs +
                ", electionTimeoutMs=" + electionTimeoutMs +
                ", logManager=" + logManager +
                ", node=" + node +
                ", timerManager=" + timerManager +
                '}';
    }

    public void setElectionTimeoutMs(long electionTimeoutMs) {
        this.electionTimeoutMs = electionTimeoutMs;
    }
}
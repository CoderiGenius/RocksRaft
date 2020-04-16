package entity;

import core.NodeImpl;
import utils.TimerManager;

/**
 * Created by 周思成 on  2020/4/14 15:33
 */

public class ReplicatorGroupOptions {

    private int               heartbeatTimeoutMs;
    private int               electionTimeoutMs;
    private LogManager        logManager;
    private BallotBox         ballotBox;
    private NodeImpl node;


    private TimerManager timerManager;

    public TimerManager getTimerManager() {
        return this.timerManager;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }



    public int getHeartbeatTimeoutMs() {
        return this.heartbeatTimeoutMs;
    }

    public void setHeartbeatTimeoutMs(int heartbeatTimeoutMs) {
        this.heartbeatTimeoutMs = heartbeatTimeoutMs;
    }

    public int getElectionTimeoutMs() {
        return this.electionTimeoutMs;
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

    public BallotBox getBallotBox() {
        return this.ballotBox;
    }

    public void setBallotBox(BallotBox ballotBox) {
        this.ballotBox = ballotBox;
    }

    public NodeImpl getNode() {
        return this.node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }


    @Override
    public String toString() {
        return "ReplicatorGroupOptions{" +
                "heartbeatTimeoutMs=" + heartbeatTimeoutMs +
                ", electionTimeoutMs=" + electionTimeoutMs +
                ", logManager=" + logManager +
                ", ballotBox=" + ballotBox +
                ", node=" + node +
                ", timerManager=" + timerManager +
                '}';
    }
}
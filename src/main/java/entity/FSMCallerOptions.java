package entity;

import core.NodeImpl;
import core.StateMachine;

/**
 * Created by 周思成 on  2020/4/22 11:12
 */

public class FSMCallerOptions {

    private LogManager   logManager;
    private StateMachine fsm;
    private Closure      afterShutdown;
    private LogId        bootstrapId;
    //private ClosureQueue closureQueue;
    private NodeImpl node;

    /**
     * disruptor buffer size.
     */
    private int          disruptorBufferSize = 1024;

    public LogManager getLogManager() {
        return logManager;
    }

    public void setLogManager(LogManager logManager) {
        this.logManager = logManager;
    }

    public StateMachine getFsm() {
        return fsm;
    }

    public void setFsm(StateMachine fsm) {
        this.fsm = fsm;
    }

    public Closure getAfterShutdown() {
        return afterShutdown;
    }

    public void setAfterShutdown(Closure afterShutdown) {
        this.afterShutdown = afterShutdown;
    }

    public LogId getBootstrapId() {
        return bootstrapId;
    }

    public void setBootstrapId(LogId bootstrapId) {
        this.bootstrapId = bootstrapId;
    }

    public NodeImpl getNode() {
        return node;
    }

    public void setNode(NodeImpl node) {
        this.node = node;
    }

    public int getDisruptorBufferSize() {
        return disruptorBufferSize;
    }

    public void setDisruptorBufferSize(int disruptorBufferSize) {
        this.disruptorBufferSize = disruptorBufferSize;
    }
}

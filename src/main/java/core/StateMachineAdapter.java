package core;

import entity.Iterator;
import entity.LeaderChangeContext;

import entity.Status;
import exceptions.RaftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/12 13:46
 */

/**
 * chule
 */
public abstract class StateMachineAdapter implements StateMachine {

    private static final Logger LOG = LoggerFactory.getLogger(StateMachineAdapter.class);
    @Override
    public void onShutdown() {
        LOG.info("Current StateMachine shutdown");
    }

    @Override
    public void onLeaderStart(long term) {
        LOG.info("Current leader start|term:"+term);
    }

    @Override
    public void onLeaderStop(Status status) {
        LOG.info("Current leader stop|status:"+status.toString());
    }

    @Override
    public void onError(RaftException e) {
        LOG.error("Current statemachine error|detail:"+e.toString());
    }

    @Override
    public void onStopFollowing(LeaderChangeContext ctx) {
        LOG.info("Current node stop following|detail:"+ctx.toString());
    }

    @Override
    public void onStartFollowing(LeaderChangeContext ctx) {
        LOG.info("Current node start following|detail:"+ctx.toString());
    }
}

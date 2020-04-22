package core;

import entity.FSMCallerOptions;
import entity.LeaderChangeContext;
import exceptions.RaftException;

/**
 * Created by 周思成 on  2020/4/22 0:48
 */

public interface FSMCaller {

    boolean init(final FSMCallerOptions opts);

    /**
     * Called when log entry committed
     *
     * @param committedIndex committed log indexx
     */
    boolean onCommitted(final long committedIndex);

    /**
     * Called when the leader starts.
     *
     * @param term current term
     */
    boolean onLeaderStart(final long term);

    /**
     * Called when start following a leader.
     *
     * @param ctx context of leader change
     */
    boolean onStartFollowing(final LeaderChangeContext ctx);

    /**
     * Called when stop following a leader.
     *
     * @param ctx context of leader change
     */
    boolean onStopFollowing(final LeaderChangeContext ctx);

    /**
     * Called when error happens.
     *
     * @param error error info
     */
    boolean onError(final RaftException error);

    /**
     * Returns the last log entry index to apply state machine.
     */
    long getLastAppliedIndex();
}

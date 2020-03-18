package entity;


import exceptions.RaftException;

/**
 * Created by 周思成 on  2020/3/11 14:44
 * @author Mike
 */

public interface StateMachine {

    /**
     * Update the StateMachine with a batch a tasks that can be accessed
     * through |iterator|.
     *
     * Invoked when one or more tasks that were passed to Node#apply(Task) have been
     * committed to the raft group (quorum of the group peers have received
     * those tasks and stored them on the backing storage).
     *
     * Once this function returns to the caller, we will regard all the iterated
     * tasks through |iter| have been successfully applied. And if you didn't
     * apply all the the given tasks, we would regard this as a critical error
     * and report a error whose type is ERROR_TYPE_STATE_MACHINE.
     *
     * @param iter iterator of states
     */
    void onApply(final Iterator iter);

    /**
     * Invoked once when the raft node was shut down.
     * Default do nothing
     */
    void onShutdown();

    /**
     * Invoked when the belonging node becomes the leader of the group at |term|
     * Default: Do nothing
     *
     * @param term new term num
     */
    void onLeaderStart(final long term);


    /**
     * Invoked when this node steps down from the leader of the replication
     * group and |status| describes detailed information
     *
     * @param status status info
     */
    void onLeaderStop(final Status status);


    /**
     * This method is called when a critical error was encountered, after this
     * point, no any further modification is allowed to applied to this node
     * until the error is fixed and this node restarts.
     *
     * @param e raft error message
     */
    void onError(final RaftException e);


    /**
     * This method is called when a follower stops following a leader and its leaderId becomes null,
     * situations including:
     * 1. handle election timeout and start preVote
     * 2. receive requests with higher term such as VoteRequest from a candidate
     *    or appendEntries request from a new leader
     * 3. receive timeoutNow request from current leader and start request vote.
     *
     * the parameter ctx gives the information(leaderId, term and status) about the
     * very leader whom the follower followed before.
     * User can reset the node's information as it stops following some leader.
     *
     * @param ctx context of leader change
     */
    void onStopFollowing(final LeaderChangeContext ctx);


    /**
     * This method is called when a follower or candidate starts following a leader and its leaderId
     * (should be NULL before the method is called) is set to the leader's id,
     * situations including:
     * 1. a candidate receives appendEntries request from a leader
     * 2. a follower(without leader) receives appendEntries from a leader
     *
     * the parameter ctx gives the information(leaderId, term and status) about
     * the very leader whom the follower starts to follow.
     * User can reset the node's information as it starts to follow some leader.
     *
     * @param ctx context of leader change
     */
    void onStartFollowing(final LeaderChangeContext ctx);

}

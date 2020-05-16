package entity;

import java.nio.ByteBuffer;

/**
 * Created by 周思成 on  2020/3/11 15:47
 * @author Mike
 */

public interface Iterator extends java.util.Iterator<ByteBuffer>{



    String getOperation();

    /**
     * Return the data whose content is the same as what was passed to
     * Node#apply(Task) in the leader node.
     */
    ByteBuffer getData();

    /**
     * Return a unique and monotonically increasing identifier of the current task:
     * - Uniqueness guarantees that committed tasks in different peers with
     *    the same index are always the same and kept unchanged.
     * - Monotonicity guarantees that for any index pair i, j (i < j), task
     *    at index |i| must be applied before task at index |j| in all the
     *    peers from the group.
     */
    long getIndex();

    /**
     * Returns the term of the leader which to task was applied to.
     */
    long getTerm();


    /**
     * If done() is non-NULL, you must call done()->Run() after applying this
     * task no matter this operation succeeds or fails, otherwise the
     * corresponding resources would leak.
     *
     * If this task is proposed by this Node when it was the leader of this
     * group and the leadership has not changed before this point, done() is
     * exactly what was passed to Node#apply(Task) which may stand for some
     * continuation (such as respond to the client) after updating the
     * StateMachine with the given task. Otherwise done() must be NULL.
     * */
    Closure done();


    /**
     * Invoked when some critical error occurred. And we will consider the last
     * |ntail| tasks (starting from the last iterated one) as not applied. After
     * this point, no further changes on the StateMachine as well as the Node
     * would be allowed and you should try to repair this replica or just drop it.
     *
     * @param ntail the number of tasks (starting from the last iterated one)  considered as not to be applied.
     * @param st    Status to describe the detail of the error.
     */
    void setErrorAndRollback(final long ntail, final Status st);

    int getSize();
}

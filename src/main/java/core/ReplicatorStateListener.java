package core;

/**
 * Created by 周思成 on  2020/4/6 0:02
 */

import entity.PeerId;
import entity.Status;

/**
 * User can implement the ReplicatorStateListener interface by themselves.
 * So they can do some their own logic codes when replicator created, destroyed or had some errors.
 *
 */
public interface ReplicatorStateListener {

    /**
     * Called when this replicator has been created.
     *
     * @param peer   replicator related peerId
     */
    void onCreated(final PeerId peer);

    /**
     * Called when this replicator has some errors.
     *
     * @param peer   replicator related peerId
     * @param status replicator's error detailed status
     */
    void onError(final PeerId peer, final Status status);

    /**
     * Called when this replicator has been destroyed.
     *
     * @param peer   replicator related peerId
     */
    void onDestroyed(final PeerId peer);
}

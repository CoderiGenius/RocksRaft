package core;

import entity.Endpoint;
import entity.Node;
import entity.PeerId;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 周思成 on  2020/3/18 12:03
 */

public interface NodeManager {

    /**
     * Return true when RPC service is registered.
     */
     boolean serverExits(final Endpoint addr);

    /**
     * Remove a RPC service address.
     */
    boolean removeAddress(final Endpoint addr);

    /**
     * Adds a RPC service address.
     */
    void addAddress(final Endpoint addr);

    /**
     * Adds a node.
     */
    boolean addNode(final Node node);
    /**
     * Remove a node.
     */
    boolean removeNode(final Node node);

    /**
     * Get node by groupId and peer.
     */
    Node get(final String groupId, final PeerId peerId);

    /**
     * Get all nodes in a raft group.
     */
     List<Node> getNodesByGroupId(final String groupId);

    /**
     * Get all nodes
     */
     List<Node> getAllNodes() ;


}

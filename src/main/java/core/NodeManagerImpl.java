package core;

import com.alipay.remoting.util.ConcurrentHashSet;
import entity.Endpoint;
import entity.Node;
import entity.NodeId;
import entity.PeerId;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by 周思成 on  2020/3/18 11:52
 * @author Mike
 */

public class NodeManagerImpl implements NodeManager {

    private static final NodeManagerImpl INSTANCE = new NodeManagerImpl();

    private final ConcurrentMap<NodeId, Node> nodeMap  = new ConcurrentHashMap<>();
    private final ConcurrentHashSet<Endpoint> addressSet = new ConcurrentHashSet<>();

    public static NodeManager getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean serverExits(Endpoint addr) {
        return this.addressSet.contains(addr);
    }

    @Override
    public boolean removeAddress(Endpoint addr) {
        return this.addressSet.remove(addr);
    }

    @Override
    public void addAddress(Endpoint addr) {
        this.addressSet.add(addr);
    }

    @Override
    public boolean addNode(Node node) {
        return false;
    }

    @Override
    public boolean removeNode(Node node) {
        return false;
    }

    @Override
    public Node get(String groupId, PeerId peerId) {
        return null;
    }

    @Override
    public List<Node> getNodesByGroupId(String groupId) {
        return null;
    }

    @Override
    public List<Node> getAllNodes() {
        return null;
    }
}

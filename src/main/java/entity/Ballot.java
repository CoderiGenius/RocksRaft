package entity;

import core.NodeImpl;
import service.ElectionService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 周思成 on  2020/3/27 19:56
 */

public class Ballot {

    List<PeerId> peerList;
    List<String> grantedPeerList;
    private AtomicInteger quorum;
    public Ballot(List<PeerId> list) {
        this.peerList = list;
        this.quorum = new AtomicInteger(this.peerList.size() / 2 + 1);
        this.grantedPeerList = new ArrayList<>(peerList.size());
    }

    public void grant(final String peerId) {

        if(findPeer(peerId)){
            this.quorum.decrementAndGet();
        }

    }

    private boolean findPeer(final String peerId) {
        for (PeerId p:
             peerList) {
            if(p.getId().equals(peerId)){
                grantedPeerList.add(peerId);
                return true;
            } else if (NodeImpl.getNodeImple().getNodeId().getPeerId().getId().equals(peerId)) {
                grantedPeerList.add(peerId);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when the ballot is granted.
     *
     * @return true if the ballot is granted
     */
    public boolean isGranted(){
        return this.quorum.get() <=0;
    }

    public List getGranted(){
        return grantedPeerList;
    }
}

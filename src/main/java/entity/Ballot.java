package entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 周思成 on  2020/3/27 19:56
 */

public class Ballot {

    List<PeerId> peerList;
    private AtomicInteger quorum;
    public Ballot(List<PeerId> list) {
        this.peerList = list;
        this.quorum = new AtomicInteger(this.peerList.size() / 2 + 1);
    }

    public void grant(final PeerId peerId) {

        if(findPeer(peerId.getId())){
            this.quorum.decrementAndGet();
        }
    }

    private boolean findPeer(final String peerId) {
        for (PeerId p:
             peerList) {
            if(p.getId().equals(peerId)){
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
}

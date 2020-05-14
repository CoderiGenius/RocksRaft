package entity;

import core.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by 周思成 on  2020/4/14 15:33
 */

public class BallotBoxForApply {
    public static final Logger LOG = LoggerFactory.getLogger(NodeImpl.class);

    enum BallotBoxState{
        //wait for follower to vote
        Voting,
        //most of the follower granted,could be applied but haven't applied yet
        Granted,
        //already been applied
        Applied
    }
    List<PeerId> peerList;
    private final AtomicReference<BallotBoxState> ballotBoxState;
    private AtomicInteger quorum;
    private final long currentIndex;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Lock writeLock = this.readWriteLock.writeLock();
    protected final Lock readLock = this.readWriteLock.readLock();

    //private final long currentTerm;
    public BallotBoxForApply(long currentIndex) {
        this.peerList = NodeImpl.getNodeImple().getPeerIdList();
        this.currentIndex = currentIndex;

        //this.currentTerm = currentTerm;
        this.quorum = new AtomicInteger(this.peerList.size() / 2 + 1);
        this.ballotBoxState = new AtomicReference<BallotBoxState>();
        this.ballotBoxState.set(BallotBoxState.Voting);
        grant(NodeImpl.getNodeImple().getNodeId().getPeerId().getId());
    }

    public void grant(final String peerId) {

        if(findPeer(peerId)){
            this.quorum.decrementAndGet();
        }
        checkBallotBoxToApply();

    }

    public void checkGranted(final String peerId,final long currentIndex, final long length) {
        if (this.currentIndex == currentIndex) {
            grant(peerId);
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
     * check if the ballot box can apply the log to the state machine
     */
    public void checkBallotBoxToApply(){

        if (isGranted() &&
                this.currentIndex == (NodeImpl.getNodeImple().getStableLogIndex().get())) {
            //check if it is sequential
            //on state machine apply
            setBallotBoxState(BallotBoxState.Applied);
            NodeImpl.getNodeImple().setStableLogIndex(currentIndex);
//            NodeImpl.getNodeImple().getFsmCaller().onCommitted(currentIndex+length);
//            NodeImpl.getNodeImple().getBallotBoxConcurrentHashMap()
//                    .get(this.currentIndex + length).checkBallotBoxToApply();

            NodeImpl.getNodeImple().setStableLogIndex(currentIndex);
        }

    }

    /**
     * Returns true when the ballot is granted.
     *
     * @return true if the ballot is granted
     */
    public boolean isGranted(){

        if (BallotBoxState.Granted.equals(getBallotBoxState()) || this.quorum.get() <= 0) {
            setBallotBoxState(BallotBoxState.Granted);
            return true;
        }
        return false;
    }

    public List<PeerId> getPeerList() {
        return peerList;
    }

    public void setPeerList(List<PeerId> peerList) {
        this.peerList = peerList;
    }

    public BallotBoxState getBallotBoxState() {
        return ballotBoxState.get();
    }

    public void setBallotBoxState(BallotBoxState ballotBoxState) {
        this.ballotBoxState.set(ballotBoxState);
    }

    public AtomicInteger getQuorum() {
        return quorum;
    }

    public void setQuorum(AtomicInteger quorum) {
        this.quorum = quorum;
    }

    public long getCurrentIndex() {
        return currentIndex;
    }



    public ReadWriteLock getReadWriteLock() {
        return readWriteLock;
    }

    public Lock getWriteLock() {
        return writeLock;
    }

    public Lock getReadLock() {
        return readLock;
    }
}

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

public class BallotBox {
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
    private final long length;
    //private final long currentTerm;
    public BallotBox(List<PeerId> list, long currentIndex, long length) {
        this.peerList = list;
        this.currentIndex = currentIndex;
        this.length = length;
        //this.currentTerm = currentTerm;
        this.quorum = new AtomicInteger(this.peerList.size() / 2 + 1);
        this.ballotBoxState = new AtomicReference<BallotBoxState>();
        this.ballotBoxState.set(BallotBoxState.Voting);
    }

    public void grant(final String peerId) {

        LOG.debug("log at {} length:{} was granted by {}",getCurrentIndex(),length,peerId);
        if(findPeer(peerId)){
            this.quorum.decrementAndGet();
        }
        checkBallotBoxToApply();

    }

    public void checkGranted(final String peerId,final long currentIndex, final long length) {
        if (this.currentIndex == currentIndex && this.length == length) {
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
     * Check if the ballot box can apply the log to the state machine
     * Rules：check if it is sequential:if it is sequential then apply
     * if it is not then mark it and wait for the front to apply
     */
    public void checkBallotBoxToApply(){

        LOG.debug("checkBallotBoxToApply applied:{} currentIndex:{} length:{} stableLogIndex:{} "
                ,getBallotBoxState(),currentIndex,length,NodeImpl.getNodeImple().getStableLogIndex());

        //1、Need to make sure that the ballot box is granted by most of peers
        //2、Need to make sure that the log is sequential. if not, wait for the front one to be applied
        //If yes, apply the current log and check the next one by the way.

        if (isGranted() &&
                this.currentIndex == (NodeImpl.getNodeImple().getStableLogIndex().get()+ 1)||
                isGranted() &&
                        //Sometimes the stable log index is equals to currentIndex. For example
                        // the startup the stable log index is 0 and currentIndex is also 0
                        this.currentIndex == (NodeImpl.getNodeImple().getStableLogIndex().get())) {
            //check if it is sequential
            //on state machine apply
            setBallotBoxState(BallotBoxState.Applied);
            NodeImpl.getNodeImple().getFsmCaller().onCommitted(currentIndex+length);
            //check next ballot box to apply
            BallotBox nextBallotBox = NodeImpl.getNodeImple().getBallotBoxConcurrentHashMap()
                    .get(this.currentIndex + length + 1);
            if (nextBallotBox != null) {
                nextBallotBox.checkBallotBoxToApply();
            }
        }else{
            // if is not sequential, wait the front one to be applied
            LOG.debug("Log granted disorder，wait the front one");

        }

    }

    /**
     * Returns true when the ballot is granted.
     *
     * @return true if the ballot is granted
     */
    public boolean isGranted(){

//        if (BallotBoxState.Granted.equals(getBallotBoxState()) || this.quorum.get() <= 0) {
//            setBallotBoxState(BallotBoxState.Granted);
//            return true;
//        }
//        return false;

        if (this.quorum.get() <= 0) {

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

    public long getLength() {
        return length;
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

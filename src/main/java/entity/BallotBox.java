package entity;

import core.NodeImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private  List<PeerId> grantList;
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Lock writeLock = this.readWriteLock.writeLock();
    protected final Lock readLock = this.readWriteLock.readLock();
    private final long length;
    //private final long currentTerm;
    public BallotBox(List<PeerId> list, long currentIndex, long length) {
        this.peerList = new ArrayList<>(list);
        this.peerList.add(NodeImpl.getNodeImple().getLeaderId().getPeerId());
        this.currentIndex = currentIndex;
        this.length = length;
        //this.currentTerm = currentTerm;
        this.quorum = new AtomicInteger((this.peerList.size()) / 2 + 1);
        this.ballotBoxState = new AtomicReference<BallotBoxState>();
        this.ballotBoxState.set(BallotBoxState.Voting);
        this.grantList =   new ArrayList<>(list.size());
        LOG.debug("BallotBox init success with quorum {}  at index {} and peerList {}"
                ,quorum,currentIndex,peerList);
    }

    public void grant(final String peerId) {

        if(findPeer(peerId)){

            this.quorum.decrementAndGet();
        }
        LOG.debug("log at {} length:{} was granted by {}, current ballot status:{}  current grant list {}"
                ,getCurrentIndex(),length,peerId,isGranted(),getGrantList());

        checkBallotBoxToApply();

    }

    public void checkGranted(final String peerId,final long currentIndex, final long length) {
        if (this.currentIndex == currentIndex && this.length == length) {
            grant(peerId);
        }else {
            LOG.error("checkGranted failed currentIndex {} but requester is {}, length {} but requester is {}"
                    ,this.currentIndex,currentIndex,this.length,length);
        }
    }

    private boolean findPeer(final String peerId) {

        for (PeerId p:
                peerList) {
            if(p.getId().equals(peerId)){
                getGrantList().add(p);
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
        //1、Need to make sure that the ballot box is granted by most of peers
        if (!isGranted()) {
            return;
        }
        LOG.warn("checkBallotBoxToApply applied:{} currentIndex:{} length:{} stableLogIndex:{} "
                ,getBallotBoxState(),currentIndex,length,NodeImpl.getNodeImple().getStableLogIndex());


        //2、Need to make sure that the log is sequential. if not, wait for the front one to be applied
        //If yes, apply the current log and check the next one by the way.

        if (
                this.currentIndex == (NodeImpl.getNodeImple().getStableLogIndex().get()+ 1)) {
            //check if it is sequential
            //on state machine apply
                apply();
        } else if (currentIndex == 0L) {

            apply();
        } else {
            // if is not sequential, wait the front one to be applied
            LOG.warn("Log granted disorder，wait the front one, current index {}", currentIndex);
            setBallotBoxState(BallotBoxState.Granted);
            LOG.warn("Check the front one, current index {}", currentIndex);
            BallotBox frontBallotBox = NodeImpl.getNodeImple().getBallotBoxConcurrentHashMap()
                    .get(this.currentIndex - length );
            if (frontBallotBox != null) {
                if(!frontBallotBox.isGranted()){
                    //the front ballotBox is still unGranted, so we need to replay the log
                    NodeImpl.getNodeImple().rePlayTheSpecificLog(frontBallotBox.getCurrentIndex());
                }
            }
        }


    }

    private void apply() {
        LOG.info("Ballot box invokes apply at index {} length {} with grant list {}"
                ,currentIndex,length,getGrantList());

        setBallotBoxState(BallotBoxState.Applied);
        NodeImpl.getNodeImple().getFsmCaller().onCommitted(currentIndex+length-1);
        //check next ballot box to apply
        BallotBox nextBallotBox = NodeImpl.getNodeImple().getBallotBoxConcurrentHashMap()
                .get(this.currentIndex + length );
        if (nextBallotBox != null) {
            nextBallotBox.checkBallotBoxToApply();
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

        return this.quorum.get() <= 0;
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

    public static Logger getLOG() {
        return LOG;
    }

    public List<PeerId> getGrantList() {
        return grantList;
    }

    public void setGrantList(List<PeerId> grantList) {
        this.grantList = grantList;
    }
}

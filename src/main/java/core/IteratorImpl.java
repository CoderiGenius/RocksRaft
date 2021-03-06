package core;

import entity.*;
import exceptions.RaftException;
import utils.Requires;
import utils.Utils;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by 周思成 on  2020/4/22 13:43
 */

public class IteratorImpl {

    private final StateMachine  fsm;
    private final LogManager logManager;
//    private final List<Closure> closures;
//    private final long          firstClosureIndex;
    private long                currentIndex;
    private final long          committedIndex;
    private LogEntry currEntry = new LogEntry(); // blank entry
    private final AtomicLong applyingIndex;
    private RaftException error;

    public IteratorImpl(final StateMachine fsm, final LogManager logManager,
                        final long lastAppliedIndex, final long committedIndex,
                        final AtomicLong applyingIndex) {
        super();
        this.fsm = fsm;
        this.logManager = logManager;
        //this.closures = closures;
        //this.firstClosureIndex = firstClosureIndex;
        this.currentIndex = lastAppliedIndex;
        this.committedIndex = committedIndex;
        this.applyingIndex = applyingIndex;
        getFirst();
    }

    @Override
    public String toString() {
        return "IteratorImpl [fsm=" + this.fsm + ", logManager=" + this.logManager
                 + ", currentIndex=" + this.currentIndex
                + ", committedIndex=" + this.committedIndex + ", currEntry=" + this.currEntry + ", applyingIndex="
                + this.applyingIndex + ", error=" + this.error + ",isGood="+isGood()+"]";
    }

    public LogEntry entry() {
        return this.currEntry;
    }

    public RaftException getError() {
        return this.error;
    }

    public boolean isGood() {
        System.out.println("current:"+currentIndex+" committedIndex:"+committedIndex);
        return this.currentIndex <= this.committedIndex && !hasError();
    }

    public boolean hasError() {
        return this.error != null;
    }

    /**
     * Move to next
     */
    public void next() {
        this.currEntry = null; //release current entry
        //get next entry
        if (this.currentIndex <= this.committedIndex) {
            ++this.currentIndex;
            if (this.currentIndex <= this.committedIndex) {
                try {

                    this.currEntry = this.logManager.getEntry(this.currentIndex);

                    if (this.currEntry == null) {

                        //getOrCreateError().setType(EnumOutter.ErrorType.ERROR_TYPE_LOG);
                        getOrCreateError().getStatus().setError(-1,
                                "Fail to get entry at index=%d while committed_index=%d", this.currentIndex,
                                this.committedIndex);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    //getOrCreateError().setType(EnumOutter.ErrorType.ERROR_TYPE_LOG);
                    getOrCreateError().getStatus().setError(RaftError.EINVAL, e.getMessage());
                }
                this.applyingIndex.set(this.currentIndex);
            }
        }
    }

    /**
     * get the first，this is useful when commitedIndex == currentIndex
     */
    public void getFirst() {
        this.currEntry = null; //release current entry
        //get next entry
        if (this.currentIndex == this.committedIndex) {


                try {

                    this.currEntry = this.logManager.getEntry(this.currentIndex);

                    if (this.currEntry == null) {

                        //getOrCreateError().setType(EnumOutter.ErrorType.ERROR_TYPE_LOG);
                        getOrCreateError().getStatus().setError(-1,
                                "Fail to get entry at index=%d while committed_index=%d", this.currentIndex,
                                this.committedIndex);
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    //getOrCreateError().setType(EnumOutter.ErrorType.ERROR_TYPE_LOG);
                    getOrCreateError().getStatus().setError(RaftError.EINVAL, e.getMessage());
                }
                this.applyingIndex.set(this.currentIndex);

        }else {
            next();
        }
    }

    public long getIndex() {
        return this.currentIndex;
    }

//    public Closure done() {
//        if (this.currentIndex < this.firstClosureIndex) {
//            return null;
//        }
//        return this.closures.get((int) (this.currentIndex - this.firstClosureIndex));
//    }

//    protected void runTheRestClosureWithError() {
//        for (long i = Math.max(this.currentIndex, this.firstClosureIndex); i <= this.committedIndex; i++) {
//            final Closure done = this.closures.get((int) (i - this.firstClosureIndex));
//            if (done != null) {
//                Requires.requireNonNull(this.error, "error");
//                Requires.requireNonNull(this.error.getStatus(), "error.status");
//                final Status status = this.error.getStatus();
//                Utils.runClosureInThread(done, status);
//            }
//        }
//    }

//    public void setErrorAndRollback(final long ntail, final Status st) {
//        Requires.requireTrue(ntail > 0, "Invalid ntail=" + ntail);
//        if (this.currEntry == null || this.currEntry.getType() != EnumOutter.EntryType.ENTRY_TYPE_DATA) {
//            this.currentIndex -= ntail;
//        } else {
//            this.currentIndex -= ntail - 1;
//        }
//        this.currEntry = null;
//        getOrCreateError().setType(EnumOutter.ErrorType.ERROR_TYPE_STATE_MACHINE);
//        getOrCreateError().getStatus().setError(RaftError.ESTATEMACHINE,
//                "StateMachine meet critical error when applying one or more tasks since index=%d, %s", this.currentIndex,
//                st != null ? st.toString() : "none");
//
//    }

    private RaftException getOrCreateError() {
        if (this.error == null) {
            this.error = new RaftException();
        }
        return this.error;
    }
}

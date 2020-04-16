package utils;

/**
 * Created by 周思成 on  2020/4/14 16:29
 */

import entity.Closure;
import entity.Status;

import java.util.concurrent.ScheduledFuture;

/**
 * A catchup closure for peer to catch up.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-04 2:15:05 PM
 */
public abstract class CatchUpClosure implements Closure {

    private long               maxMargin;
    private ScheduledFuture<?> timer;
    private boolean            hasTimer;
    private boolean            errorWasSet;

    private final Status status = Status.OK();

    public Status getStatus() {
        return this.status;
    }

    public long getMaxMargin() {
        return this.maxMargin;
    }

    public void setMaxMargin(long maxMargin) {
        this.maxMargin = maxMargin;
    }

    public ScheduledFuture<?> getTimer() {
        return this.timer;
    }

    public void setTimer(ScheduledFuture<?> timer) {
        this.timer = timer;
        this.hasTimer = true;
    }

    public boolean hasTimer() {
        return this.hasTimer;
    }

    public boolean isErrorWasSet() {
        return this.errorWasSet;
    }

    public void setErrorWasSet(boolean errorWasSet) {
        this.errorWasSet = errorWasSet;
    }
}
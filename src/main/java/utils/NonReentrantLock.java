package utils;

/**
 * Created by 周思成 on  2020/4/14 16:21
 */

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Non reentrant lock, copied from netty project.
 */
public final class NonReentrantLock extends AbstractQueuedSynchronizer implements Lock {

    private static final long serialVersionUID = -833780837233068610L;

    private Thread            owner;

    @Override
    public void lock() {
        acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return tryAcquire(1);
    }

    @Override
    public boolean tryLock(final long time, final TimeUnit unit) throws InterruptedException {
        return tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        release(1);
    }

    public boolean isHeldByCurrentThread() {
        return isHeldExclusively();
    }

    public Thread getOwner() {
        return this.owner;
    }

    @Override
    public Condition newCondition() {
        return new ConditionObject();
    }

    @Override
    protected boolean tryAcquire(final int acquires) {
        if (compareAndSetState(0, 1)) {
            this.owner = Thread.currentThread();
            return true;
        }
        return false;
    }

    @Override
    protected boolean tryRelease(final int releases) {
        if (Thread.currentThread() != this.owner) {
            throw new IllegalMonitorStateException("Owner is " + this.owner);
        }
        this.owner = null;
        setState(0);
        return true;
    }

    @Override
    protected boolean isHeldExclusively() {
        return getState() != 0 && this.owner == Thread.currentThread();
    }
}


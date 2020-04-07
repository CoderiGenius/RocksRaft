package utils;

/**
 * Created by 周思成 on  2020/4/5 23:49
 */

import com.alipay.remoting.NamedThreadFactory;
import core.Lifecycle;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The global timer manager.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Mar-30 3:24:34 PM
 */
public class TimerManager implements Lifecycle<Integer> {

    private ScheduledExecutorService executor;

    @Override
    public boolean init(Integer coreSize) {
        this.executor = Executors.newScheduledThreadPool(coreSize, new NamedThreadFactory(
                "JRaft-Node-ScheduleThreadPool-", true));
        return true;
    }

    @Override
    public void shutdown() {
        if (this.executor != null) {
            this.executor.shutdownNow();
            this.executor = null;
        }
    }

    private void checkStarted() {
        if (this.executor == null) {
            throw new IllegalStateException("Please init timer manager.");
        }
    }

    public ScheduledFuture<?> schedule(final Runnable command, final long delay, final TimeUnit unit) {
        checkStarted();
        return this.executor.schedule(command, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(final Runnable command, final long initialDelay, final long period,
                                                  final TimeUnit unit) {
        checkStarted();
        return this.executor.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(final Runnable command, final long initialDelay, final long delay,
                                                     final TimeUnit unit) {
        checkStarted();
        return this.executor.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}


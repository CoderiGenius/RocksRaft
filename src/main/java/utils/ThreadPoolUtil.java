package utils;

import java.util.concurrent.*;

/**
 * Created by 周思成 on  2020/3/13 22:08
 */

public class ThreadPoolUtil {

    /**
     * The default rejected execution handler
     */
    private static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy();

    public static PoolBuilder newBuilder() {
        return new PoolBuilder();
    }


    /**
     * Creates a new {@code MetricThreadPoolExecutor} or {@code LogThreadPoolExecutor}
     * with the given initial parameters.
     *
     * @param poolName         the name of the thread pool
     * @param coreThreads      the number of threads to keep in the pool, even if they are
     *                         idle, unless {@code allowCoreThreadTimeOut} is set.
     * @param maximumThreads   the maximum number of threads to allow in the pool
     * @param keepAliveSeconds when the number of threads is greater than the core, this
     *                         is the maximum time (seconds) that excess idle threads will
     *                         wait for new tasks before terminating.
     * @param workQueue        the queue to use for holding tasks before they are executed.
     *                         This queue will hold only the {@code Runnable} tasks submitted
     *                         by the {@code execute} method.
     * @param threadFactory    the factory to use when the executor creates a new thread
     * @param handler          the handler to use when execution is blocked because the
     *                         thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveSeconds < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code threadFactory} or {@code handler} is null
     */
    public static ThreadPoolExecutor newThreadPool(final String poolName,
                                                   final int coreThreads, final int maximumThreads,
                                                   final long keepAliveSeconds,
                                                   final BlockingQueue<Runnable> workQueue,
                                                   final ThreadFactory threadFactory,
                                                   final RejectedExecutionHandler handler) {
        final TimeUnit unit = TimeUnit.SECONDS;

            return new LogThreadPoolExecutor(coreThreads, maximumThreads, keepAliveSeconds, unit, workQueue,
                    threadFactory, handler, poolName);

    }


    private ThreadPoolUtil() {
    }


    public static class PoolBuilder {
        private String                   poolName;

        private Integer                  coreThreads;
        private Integer                  maximumThreads;
        private Long                     keepAliveSeconds;
        private BlockingQueue<Runnable>  workQueue;
        private ThreadFactory            threadFactory;
        private RejectedExecutionHandler handler = ThreadPoolUtil.defaultHandler;

        public PoolBuilder poolName(final String poolName) {
            this.poolName = poolName;
            return this;
        }



        public PoolBuilder coreThreads(final Integer coreThreads) {
            this.coreThreads = coreThreads;
            return this;
        }

        public PoolBuilder maximumThreads(final Integer maximumThreads) {
            this.maximumThreads = maximumThreads;
            return this;
        }

        public PoolBuilder keepAliveSeconds(final Long keepAliveSeconds) {
            this.keepAliveSeconds = keepAliveSeconds;
            return this;
        }

        public PoolBuilder workQueue(final BlockingQueue<Runnable> workQueue) {
            this.workQueue = workQueue;
            return this;
        }

        public PoolBuilder threadFactory(final ThreadFactory threadFactory) {
            this.threadFactory = threadFactory;
            return this;
        }

        public PoolBuilder rejectedHandler(final RejectedExecutionHandler handler) {
            this.handler = handler;
            return this;
        }

        public ThreadPoolExecutor build() {
            Requires.requireNonNull(this.poolName, "poolName");

            Requires.requireNonNull(this.coreThreads, "coreThreads");
            Requires.requireNonNull(this.maximumThreads, "maximumThreads");
            Requires.requireNonNull(this.keepAliveSeconds, "keepAliveSeconds");
            Requires.requireNonNull(this.workQueue, "workQueue");
            Requires.requireNonNull(this.threadFactory, "threadFactory");
            Requires.requireNonNull(this.handler, "handler");

            return ThreadPoolUtil.newThreadPool(this.poolName, this.coreThreads,
                    this.maximumThreads, this.keepAliveSeconds, this.workQueue, this.threadFactory, this.handler);
        }
    }
}

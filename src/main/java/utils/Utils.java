package utils;

import com.alipay.remoting.NamedThreadFactory;
import io.netty.util.internal.SystemPropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.regex.Pattern;

/**
 * Created by 周思成 on  2020/3/10 15:18
 * @author Mike
 */

public class Utils {


    /**
     * ANY IP address 0.0.0.0
     */
    public static final String IP_ANY = "0.0.0.0";


    private static final Logger LOG                                 = LoggerFactory.getLogger(Utils.class);

    /**
     * The configured number of available processors. The default is {@link Runtime#availableProcessors()}.
     * This can be overridden by setting the system property "jraft.available_processors".
     */
    private static final int          CPUS                                = SystemPropertyUtil.getInt(
            "jraft.available_processors", Runtime
                    .getRuntime().availableProcessors());

    /**
     * Default jraft closure executor pool minimum size, CPUs by default.
     */
    public static final int           MIN_CLOSURE_EXECUTOR_POOL_SIZE      = SystemPropertyUtil.getInt(
            "jraft.closure.threadpool.size.min",
            cpus());

    /**
     * Default jraft closure executor pool maximum size.
     */
    public static final int           MAX_CLOSURE_EXECUTOR_POOL_SIZE      = SystemPropertyUtil.getInt(
            "jraft.closure.threadpool.size.max",
            Math.max(100, cpus() * 5));

    /**
     * Default jraft append-entries executor(send) pool size.
     */
    public static final int           APPEND_ENTRIES_THREADS_SEND         = SystemPropertyUtil
            .getInt(
                    "jraft.append.entries.threads.send",
                    Math.max(
                            16,
                            Ints.findNextPositivePowerOfTwo(cpus() * 2)));

    /**
     * Default jraft max pending tasks of append-entries per thread, 65536 by default.
     */
    public static final int           MAX_APPEND_ENTRIES_TASKS_PER_THREAD = SystemPropertyUtil
            .getInt(
                    "jraft.max.append.entries.tasks.per.thread",
                    32768);

    /**
     * Whether use {@link com.alipay.sofa.jraft.util.concurrent.MpscSingleThreadExecutor}, true by default.
     */
    public static final boolean       USE_MPSC_SINGLE_THREAD_EXECUTOR     = SystemPropertyUtil.getBoolean(
            "jraft.use.mpsc.single.thread.executor",
            true);

    /**
     * Global thread pool to run closure.
     */
    private static ThreadPoolExecutor CLOSURE_EXECUTOR                    = ThreadPoolUtil
            .newBuilder()
            .poolName("JRAFT_CLOSURE_EXECUTOR")

            .coreThreads(
                    MIN_CLOSURE_EXECUTOR_POOL_SIZE)
            .maximumThreads(
                    MAX_CLOSURE_EXECUTOR_POOL_SIZE)
            .keepAliveSeconds(60L)
            .workQueue(new SynchronousQueue<>())
            .threadFactory(
                    new NamedThreadFactory(
                            "JRaft-Closure-Executor-", true))
            .build();

    private static final Pattern GROUP_ID_PATTER                     = Pattern
            .compile("^[a-zA-Z][a-zA-Z0-9\\-_]*$");

    /**
     * Get system CPUs count.
     */
    public static int cpus() {
        return CPUS;
    }

}

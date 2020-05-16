package rpc;

import core.NodeImpl;
import core.ReadIteratorImpl;
import entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TimerManager;
import utils.Utils;

import java.util.List;
import java.util.concurrent.*;

/**
 * Created by 周思成 on  2020/5/15 1:08
 */

public class EnClosureClientRpcRequest {

    private final ClientRpcService clientRpcService;
    private final ThreadPoolExecutor clientRpcThreadPool;
    private TimerManager timerManager;
    private final long retryDelay = 500;
    private final long WAITDELAY = 50;
    public static final Logger LOG = LoggerFactory.getLogger(EnClosureClientRpcRequest.class);

    public EnClosureClientRpcRequest(ClientRpcService clientRpcService) {
        this.clientRpcThreadPool = new ThreadPoolExecutor(
                Utils.APPEND_ENTRIES_THREADS_SEND,
                Utils.APPEND_ENTRIES_THREADS_SEND,
                10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadFactoryImpl()
                , new RejectedExecutionHandlerImpl());
        this.clientRpcService = clientRpcService;
        timerManager = new TimerManager();
        timerManager.init(Utils.APPEND_ENTRIES_THREADS_SEND);
    }
    private static class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOG.error("EnClosureTaskRpcRequest ThreadPool error:rejected");
        }
    }
    private static class ThreadFactoryImpl implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"EnClosureTaskRpcRequest-");

        }
    }

//    class ReadClousure implements Closure{
//
//        @Override
//        public void run(Status status) {
//
//        }
//    }

    public Future<RpcResult> warpApplyToStateMachine(ReadTaskSchedule readTaskSchedule,
                                                     boolean retry) {

        ReadIteratorImpl readIterator = new ReadIteratorImpl(readTaskSchedule.getReadEventList());
        RpcResult rpcResult = RpcResult.newRpcResult();
        return handleApplyToStateMachine(readIterator,readTaskSchedule,retry,rpcResult);
    }


    public Future<RpcResult> handleApplyToStateMachine(ReadIteratorImpl readIterator,
                                                       ReadTaskSchedule readTaskSchedule,
                                                       boolean retry,RpcResult rpcResult) {

        Callable<RpcResult> callable = () -> {

            if(NodeImpl.getNodeImple().getStableLogIndex().get() >= readTaskSchedule.getIndex()) {
                try {


                    NodeImpl.getNodeImple().getStateMachine().onApply(readIterator);

                    rpcResult.setSuccess(true);
                    return rpcResult;
                } catch (Exception e) {
                    rpcResult.setSuccess(false);
                    LOG.error("handleNotifyClientRequest EnClosure error {}", e.getMessage());
                    if (retry) {
                        getTimerManager().schedule(() -> handleApplyToStateMachine(
                                readIterator,readTaskSchedule, false,rpcResult),
                                retryDelay, TimeUnit.MILLISECONDS);
                    }
                }

            }else {
                getTimerManager().schedule(() -> handleApplyToStateMachine(
                        readIterator,readTaskSchedule, true,rpcResult),
                        WAITDELAY, TimeUnit.MILLISECONDS);

            }
            return rpcResult;
        };
        return getClientRpcThreadPool().submit(callable);

    }



    public Future<RpcResult> handleNotifyClient(List<ReadTask> list,
                                                       boolean retry,RpcResult rpcResult ) {

        Callable<RpcResult> callable = () -> {


                try {

                    getClientRpcService().readResults(list);
                    rpcResult.setSuccess(true);
                    return rpcResult;
                } catch (Exception e) {
                    rpcResult.setSuccess(false);
                    LOG.error("handleNotifyClientRequest EnClosure error {}", e.getMessage());
                    if (retry) {
                        getTimerManager().schedule(() -> handleNotifyClient(
                                list, false,rpcResult),
                                retryDelay, TimeUnit.MILLISECONDS);
                    }
                }

            return rpcResult;
        };
        return getClientRpcThreadPool().submit(callable);

    }



    public ClientRpcService getClientRpcService() {
        return clientRpcService;
    }

    public ThreadPoolExecutor getClientRpcThreadPool() {
        return clientRpcThreadPool;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    public long getRetryDelay() {
        return retryDelay;
    }

    public static Logger getLOG() {
        return LOG;
    }
}

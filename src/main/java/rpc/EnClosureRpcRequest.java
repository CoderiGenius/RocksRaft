package rpc;

import core.NodeImpl;
import core.ReplicatorGroupImpl;
import entity.Endpoint;
import entity.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.TimerManager;
import utils.Utils;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by 周思成 on  2020/4/24 16:49
 */

public class EnClosureRpcRequest  {


    private Map<Endpoint, RpcServices> rpcServicesMap = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor replicatorGroupThreadPool;
    private TimerManager timerManager;
    private final long retryDelay = 500;
    public EnClosureRpcRequest(Map<Endpoint, RpcServices> rpcServicesMap) {
        this.rpcServicesMap = rpcServicesMap;
        replicatorGroupThreadPool = new ThreadPoolExecutor(
                Utils.APPEND_ENTRIES_THREADS_SEND,
                Utils.APPEND_ENTRIES_THREADS_SEND,
                10L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
                new ThreadFactoryImpl(),new RejectedExecutionHandlerImpl());
        timerManager = new TimerManager();
        timerManager.init(Utils.APPEND_ENTRIES_THREADS_SEND);
    }

    public static final Logger LOG = LoggerFactory.getLogger(EnClosureRpcRequest.class);



    private class RejectedExecutionHandlerImpl implements RejectedExecutionHandler{

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOG.error("EnClosureRpcRequest ThreadPool error:rejected");
        }
    }
    private class ThreadFactoryImpl implements ThreadFactory {

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"EnClosureRpcRequest-");

        }
    }



    public Future<RpcResult> handlePreVoteRequest(RpcRequests.RequestPreVoteRequest preVoteRequest,
                                                  Endpoint endpoint,boolean retry) {

        Callable<RpcResult> callable = () -> {
           RpcResult rpcResult = RpcResult.newRpcResult();
            try {
                getRpcServicesMap().get(endpoint).handlePreVoteRequest(preVoteRequest);
                rpcResult.setSuccess(true);
                return rpcResult;
            } catch (Exception e) {
                rpcResult.setSuccess(false);
                LOG.error("handlePreVoteRequest EnClosure error {}",e.getMessage());
                if (retry) {
                    getTimerManager().schedule(()->handlePreVoteRequest(preVoteRequest,endpoint,false),
                            retryDelay,TimeUnit.MILLISECONDS);
                }

            }
            return rpcResult;
        };
       return getReplicatorGroupThreadPool().submit(callable);

    }


    public Future<RpcResult> handleVoteRequest(RpcRequests.RequestVoteRequest requestVoteRequest,
                                                Endpoint endpoint,boolean retry) {
        Callable<RpcResult> callable = () -> {
            RpcResult rpcResult = RpcResult.newRpcResult();
            try {
                getRpcServicesMap().get(endpoint).handleVoteRequest(requestVoteRequest);
                rpcResult.setSuccess(true);
                return rpcResult;
            } catch (Exception e) {
                rpcResult.setSuccess(false);
                LOG.error("handleVoteRequest EnClosure error {}",e.getMessage());
                if (retry) {
                    getTimerManager().schedule(()->handleVoteRequest(requestVoteRequest,endpoint,false),
                            retryDelay,TimeUnit.MILLISECONDS);
                }
            }
            return rpcResult;
        };
        return getReplicatorGroupThreadPool().submit(callable);
    }


    public Future<RpcResult>  handleApendEntriesRequest(RpcRequests.AppendEntriesRequest appendEntriesRequest,
                                                        RpcServices rpcServices,boolean retry) {
        Callable<RpcResult> callable = () -> {
            RpcResult rpcResult = RpcResult.newRpcResult();
            try {
                rpcServices.handleApendEntriesRequest(appendEntriesRequest);
                rpcResult.setSuccess(true);
                return rpcResult;
            } catch (Exception e) {
                rpcResult.setSuccess(false);
                LOG.error("handleApendEntriesRequest EnClosure error {}",e.getMessage());
                if (retry) {
                    getTimerManager().schedule(()->handleApendEntriesRequest(appendEntriesRequest,
                            rpcServices,false),
                            retryDelay,TimeUnit.MILLISECONDS);
                }
            }
            return rpcResult;
        };
        return getReplicatorGroupThreadPool().submit(callable);
    }


    public Future<RpcResult>  handleApendEntriesRequests(RpcRequests.AppendEntriesRequests appendEntriesRequests,
                                                         RpcServices rpcServices,boolean retry) {
        Callable<RpcResult> callable = () -> {
            RpcResult rpcResult = RpcResult.newRpcResult();
            try {
                rpcServices.handleApendEntriesRequests(appendEntriesRequests);
                rpcResult.setSuccess(true);
                return rpcResult;
            } catch (Exception e) {
                rpcResult.setSuccess(false);
                LOG.error("handleApendEntriesRequests EnClosure error {}",e.getMessage());
                if (retry) {
                    getTimerManager().schedule(()->handleApendEntriesRequests(appendEntriesRequests,
                            rpcServices,false),
                            retryDelay,TimeUnit.MILLISECONDS);
                }
            }
            return rpcResult;
        };
        return getReplicatorGroupThreadPool().submit(callable);
    }


    public Future<RpcResult>  handleFollowerStableRequest(
            RpcRequests.NotifyFollowerStableRequest notifyFollowerStableRequest,Endpoint endpoint,boolean retry) {
        Callable<RpcResult> callable = () -> {
            RpcResult rpcResult = RpcResult.newRpcResult();
            try {
                getRpcServicesMap().get(endpoint).handleFollowerStableRequest(notifyFollowerStableRequest);
                rpcResult.setSuccess(true);
                return rpcResult;
            } catch (Exception e) {
                rpcResult.setSuccess(false);
                LOG.error("handleFollowerStableRequest EnClosure error {}",e.getMessage());
                if (retry) {
                    getTimerManager().schedule(()->handleFollowerStableRequest(notifyFollowerStableRequest,
                            endpoint,false),
                            retryDelay,TimeUnit.MILLISECONDS);
                }
            }
            return rpcResult;
        };
        return getReplicatorGroupThreadPool().submit(callable);
    }


    public Map<Endpoint, RpcServices> getRpcServicesMap() {
        return rpcServicesMap;
    }

    public void setRpcServicesMap(Map<Endpoint, RpcServices> rpcServicesMap) {
        this.rpcServicesMap = rpcServicesMap;
    }

    public ThreadPoolExecutor getReplicatorGroupThreadPool() {
        return replicatorGroupThreadPool;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }
}

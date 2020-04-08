package entity;

/**
 * Created by 周思成 on  2020/3/11 14:30
 */

import service.RaftServiceFactory;
import utils.Utils;

import java.util.ServiceLoader;

/**
 * Node options.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-04 2:59:12 PM
 */
public class NodeOptions extends RpcOptions {

    public static NodeOptions nodeOptions  = new NodeOptions();

    public static NodeOptions getNodeOptions() {
        return nodeOptions;
    }

    private NodeOptions(){};

    public static final RaftServiceFactory defaultServiceFactory  =
            ServiceLoader.load(RaftServiceFactory.class).iterator().next();

    // A follower would become a candidate if it doesn't receive any message
    // from the leader in |election_timeout_ms| milliseconds
    // follower to candidate timeout
    private long electionTimeOut;

    // One node's local priority value would be set to | electionPriority |
    // value when it starts up.If this value is set to 0,the node will never be a leader.
    // If this node doesn't support priority election,then set this value to -1.
    // Default: -1
    //private int                             electionPriority       = ElectionPriority.Disabled;









//    // If node is starting from a empty environment (both LogStorage and
//    // SnapshotStorage are empty), it would use |initial_conf| as the
//    // configuration of the group, otherwise it would load configuration from
//    // the existing environment.
//    //
//    // Default: A empty group
//    private Configuration initialConf            = new Configuration();

    // The specific StateMachine implemented your business logic, which must be
    // a valid instance.
    private StateMachine                    fsm;

    // Describe a specific LogStorage in format ${type}://${parameters}
    private String                          logUri;


    private String rpcProtocol;
    private String serialization;
    private int port;
    private boolean daemon;
    private String rpcServiceName;

    private String taskExecuteMethod;


    // If non-null, we will pass this throughput_snapshot_throttle to SnapshotExecutor
    // Default: NULL
    //    scoped_refptr<SnapshotThrottle>* snapshot_throttle;

    // If true, RPCs through raft_cli will be denied.
    // Default: false
    private boolean                         disableCli             = false;

    /**
     * Timer manager thread pool size
     */
    private int                             timerPoolSize          = Utils.cpus() * 3 > 20 ? 20 : Utils.cpus() * 3;

    /**
     * CLI service request RPC.proto executor pool size, use default executor if -1.
     */
    private int                             cliRpcThreadPoolSize   = Utils.cpus();
    /**
     * RAFT request RPC.proto executor pool size, use default executor if -1.
     */
    private int                             raftRpcThreadPoolSize  = Utils.cpus() * 6;

    //心跳包最长时间
    private long maxHeartBeatTime;

    //选举超时时间
    private long maxElectionTime;

    /**
     * Custom service factory.
     */
    private RaftServiceFactory             serviceFactory         = defaultServiceFactory;


    private int taskPort;

    public String getTaskExecuteMethod() {
        return taskExecuteMethod;
    }

    public void setTaskExecuteMethod(String taskExecuteMethod) {
        this.taskExecuteMethod = taskExecuteMethod;
    }

    public int getTaskPort() {
        return taskPort;
    }

    public void setTaskPort(int taskPort) {
        this.taskPort = taskPort;
    }

    public long getElectionTimeOut() {
        return electionTimeOut;
    }

    public void setElectionTimeOut(long electionTimeOut) {
        this.electionTimeOut = electionTimeOut;
    }

    public long getMaxHeartBeatTime() {
        return maxHeartBeatTime;
    }

    public void setMaxHeartBeatTime(long maxHeartBeatTime) {
        this.maxHeartBeatTime = maxHeartBeatTime;
    }

    public long getMaxElectionTime() {
        return maxElectionTime;
    }

    public void setMaxElectionTime(long maxElectionTime) {
        this.maxElectionTime = maxElectionTime;
    }

    public String getRpcServiceName() {
        return rpcServiceName;
    }

    public void setRpcServiceName(String rpcServiceName) {
        this.rpcServiceName = rpcServiceName;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isDaemon() {
        return daemon;
    }

    public void setDaemon(boolean daemon) {
        this.daemon = daemon;
    }

    public static RaftServiceFactory getDefaultServiceFactory() {
        return defaultServiceFactory;
    }







    public StateMachine getFsm() {
        return fsm;
    }

    public void setFsm(StateMachine fsm) {
        this.fsm = fsm;
    }

    public String getLogUri() {
        return logUri;
    }

    public void setLogUri(String logUri) {
        this.logUri = logUri;
    }

    public String getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(String rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
    }

    public boolean isDisableCli() {
        return disableCli;
    }

    public void setDisableCli(boolean disableCli) {
        this.disableCli = disableCli;
    }

    public int getTimerPoolSize() {
        return timerPoolSize;
    }

    public void setTimerPoolSize(int timerPoolSize) {
        this.timerPoolSize = timerPoolSize;
    }

    public int getCliRpcThreadPoolSize() {
        return cliRpcThreadPoolSize;
    }

    public void setCliRpcThreadPoolSize(int cliRpcThreadPoolSize) {
        this.cliRpcThreadPoolSize = cliRpcThreadPoolSize;
    }

    public int getRaftRpcThreadPoolSize() {
        return raftRpcThreadPoolSize;
    }

    public void setRaftRpcThreadPoolSize(int raftRpcThreadPoolSize) {
        this.raftRpcThreadPoolSize = raftRpcThreadPoolSize;
    }

    public RaftServiceFactory getServiceFactory() {
        return serviceFactory;
    }

    public void setServiceFactory(RaftServiceFactory serviceFactory) {
        this.serviceFactory = serviceFactory;
    }
}

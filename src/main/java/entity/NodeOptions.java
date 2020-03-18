package entity;

/**
 * Created by 周思成 on  2020/3/11 14:30
 */

import config.Configuration;
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

    public static final RaftServiceFactory defaultServiceFactory  =
            ServiceLoader.load(RaftServiceFactory.class).iterator().next();

    // A follower would become a candidate if it doesn't receive any message
    // from the leader in |election_timeout_ms| milliseconds
    // Default: 1000 (1s)
    // follower to candidate timeout
    private int                             electionTimeoutMs      = 1000;

    // One node's local priority value would be set to | electionPriority |
    // value when it starts up.If this value is set to 0,the node will never be a leader.
    // If this node doesn't support priority election,then set this value to -1.
    // Default: -1
    //private int                             electionPriority       = ElectionPriority.Disabled;

    // If next leader is not elected until next election timeout, it exponentially
    // decay its local target priority, for example target_priority = target_priority - gap
    // Default: 10
    private int                             decayPriorityGap       = 10;

    // Leader lease time's ratio of electionTimeoutMs,
    // To minimize the effects of clock drift, we should make that:
    // clockDrift + leaderLeaseTimeoutMs < electionTimeout
    // Default: 90, Max: 100
    private int                             leaderLeaseTimeRatio   = 90;

    // A snapshot saving would be triggered every |snapshot_interval_s| seconds
    // if this was reset as a positive number
    // If |snapshot_interval_s| <= 0, the time based snapshot would be disabled.
    //
    // Default: 3600 (1 hour)
    private int                             snapshotIntervalSecs   = 3600;

    // A snapshot saving would be triggered every |snapshot_interval_s| seconds,
    // and at this moment when state machine's lastAppliedIndex value
    // minus lastSnapshotId value is greater than snapshotLogIndexMargin value,
    // the snapshot action will be done really.
    // If |snapshotLogIndexMargin| <= 0, the distance based snapshot would be disable.
    //
    // Default: 0
    private int                             snapshotLogIndexMargin = 0;

    // We will regard a adding peer as caught up if the margin between the
    // last_log_index of this peer and the last_log_index of leader is less than
    // |catchup_margin|
    //
    // Default: 1000
    private int                             catchupMargin          = 1000;

    // If node is starting from a empty environment (both LogStorage and
    // SnapshotStorage are empty), it would use |initial_conf| as the
    // configuration of the group, otherwise it would load configuration from
    // the existing environment.
    //
    // Default: A empty group
    private Configuration initialConf            = new Configuration();

    // The specific StateMachine implemented your business logic, which must be
    // a valid instance.
    private StateMachine                    fsm;

    // Describe a specific LogStorage in format ${type}://${parameters}
    private String                          logUri;




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




    /**
     * Custom service factory.
     */
    private RaftServiceFactory             serviceFactory         = defaultServiceFactory;


}

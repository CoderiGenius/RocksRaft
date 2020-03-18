package entity;

/**
 * Created by 周思成 on  2020/3/11 14:31
 */

public class RpcOptions {


    /**
     * Rpc connect timeout in milliseconds
     * Default: 1000(1s)
     */
    private int            rpcConnectTimeoutMs        = 1000;

    /**
     * RPC.proto request default timeout in milliseconds
     * Default: 5000(5s)
     */
    private int            rpcDefaultTimeout          = 5000;

    /**
     * Install snapshot RPC.proto request default timeout in milliseconds
     * Default: 5 * 60 * 1000(5min)
     */
    private int            rpcInstallSnapshotTimeout  = 5 * 60 * 1000;

    /**
     * RPC.proto process thread pool size
     * Default: 80
     */
    private int            rpcProcessorThreadPoolSize = 80;

    /**
     * Whether to enable checksum for RPC.proto.
     * Default: false
     */
    private boolean        enableRpcChecksum          = false;


    public int getRpcConnectTimeoutMs() {
        return rpcConnectTimeoutMs;
    }

    public void setRpcConnectTimeoutMs(int rpcConnectTimeoutMs) {
        this.rpcConnectTimeoutMs = rpcConnectTimeoutMs;
    }

    public int getRpcDefaultTimeout() {
        return rpcDefaultTimeout;
    }

    public void setRpcDefaultTimeout(int rpcDefaultTimeout) {
        this.rpcDefaultTimeout = rpcDefaultTimeout;
    }

    public int getRpcInstallSnapshotTimeout() {
        return rpcInstallSnapshotTimeout;
    }

    public void setRpcInstallSnapshotTimeout(int rpcInstallSnapshotTimeout) {
        this.rpcInstallSnapshotTimeout = rpcInstallSnapshotTimeout;
    }

    public int getRpcProcessorThreadPoolSize() {
        return rpcProcessorThreadPoolSize;
    }

    public void setRpcProcessorThreadPoolSize(int rpcProcessorThreadPoolSize) {
        this.rpcProcessorThreadPoolSize = rpcProcessorThreadPoolSize;
    }

    public boolean isEnableRpcChecksum() {
        return enableRpcChecksum;
    }

    public void setEnableRpcChecksum(boolean enableRpcChecksum) {
        this.enableRpcChecksum = enableRpcChecksum;
    }
}

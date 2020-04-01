package entity;

/**
 * Created by 周思成 on  2020/3/30 21:28
 * @author Mike
 */

public class CurrentNodeOptions{
    private long electionTimeOut;
    private long maxHeartBeatTime;
    private String rpcProtocol;
    private String serialization;
    private int port;
    private boolean daemon;
    private String groupId;
    private String address;
    private String name;
    private String peerId;


    @Override
    public String toString() {
        return "CurrentNodeOptions{" +
                "electionTimeOut=" + electionTimeOut +
                ", maxHeartBeatTime=" + maxHeartBeatTime +
                ", rpcProtocol='" + rpcProtocol + '\'' +
                ", serialization='" + serialization + '\'' +
                ", port=" + port +
                ", daemon=" + daemon +
                ", groupId='" + groupId + '\'' +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", peerId='" + peerId + '\'' +
                '}';
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

    public String getRpcProtocol() {
        return rpcProtocol;
    }

    public void setRpcProtocol(String rpcProtocol) {
        this.rpcProtocol = rpcProtocol;
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

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }
}
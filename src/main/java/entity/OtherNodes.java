package entity;

/**
 * Created by 周思成 on  2020/3/30 21:25
 */

public  class OtherNodes{
    private String peerId;
    private String name;
    private String address;
    private Integer port;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    @Override
    public String toString() {
        return "OtherNodes{" +
                "peerId='" + peerId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
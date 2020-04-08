package entity;

import java.io.Serializable;

import com.alipay.remoting.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.AsciiStringUtil;
import utils.CrcUtil;
import utils.Utils;




/**
 * Represent a participant in a replicating group.
 * Created by 周思成 on  2020/3/10 14:51
 */


public class PeerId implements Serializable,Checksum {

    private static final Logger LOG              = LoggerFactory.getLogger(PeerId.class);

    /** Peer address. */
    private Endpoint endpoint;


    public static final PeerId  ANY_PEER         = new PeerId();


    private long                checksum;

    /**
     * peerName and id are equally the same
     */
    private String peerName;
    private String id;

    private int taskPort;
    public PeerId() {

    }

    public int getTaskPort() {
        return taskPort;
    }

    public void setTaskPort(int taskPort) {
        this.taskPort = taskPort;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PeerId(String id,String peerName,String address,Integer port,Integer taskPort) {
            this.endpoint = new Endpoint(address,port);
            this.id = id;
            this.peerName = peerName;
            this.taskPort = taskPort;
    }

    public PeerId copy() {
        return new PeerId();
    }
    @Override
    public long checksum() {
        if (this.checksum == 0) {
            this.checksum = CrcUtil.crc64(AsciiStringUtil.unsafeEncode(toString()));
        }
        return this.checksum;
    }


    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    /**
     * Create an empty peer.
     * @return empty peer
     */
    public static PeerId emptyPeer() {
        return new PeerId();
    }


    public PeerId(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public static Logger getLOG() {
        return LOG;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public static PeerId getAnyPeer() {
        return ANY_PEER;
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }


    @Override
    public String toString() {
        return "PeerId{" +
                "endpoint=" + endpoint +
                ", checksum=" + checksum +
                '}';
    }

    /**
     * Parse peerId from string that generated by {@link #toString()}
     * This method can support parameter string values are below:
     *
     * <pre>
     * PeerId.parse("a:b")          = new PeerId("a", "b", 0 , -1)
     * PeerId.parse("a:b:c")        = new PeerId("a", "b", "c", -1)
     * PeerId.parse("a:b::d")       = new PeerId("a", "b", 0, "d")
     * PeerId.parse("a:b:c:d")      = new PeerId("a", "b", "c", "d")
     * </pre>
     *
     */
    public boolean parse(final String s) {
        if (StringUtils.isEmpty(s)) {
            return false;
        }

        final String[] tmps = StringUtils.split(s, ':');
        if (tmps.length < 2 ) {
            return false;
        }
        try {
            final int port = Integer.parseInt(tmps[1]);
            this.endpoint = new Endpoint(tmps[0], port);


            return true;
        } catch (final Exception e) {
            LOG.error("Parse peer from string failed: {}.", s, e);
            return false;
        }
    }
}

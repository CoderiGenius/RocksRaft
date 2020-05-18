package entity;

import com.google.protobuf.ZeroByteStringHelper;
import rpc.EnumOutter;
import utils.CrcUtil;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

/**
 * Created by 周思成 on  2020/3/13 16:37
 */

public class LogEntry implements Checksum {
    /** entry type */
    private EnumOutter.EntryType type;
    /** log id with index/term */
    private LogId                id = new LogId(0, 0);
    private PeerId leaderId;
    /** log entry current peers */
    private List<PeerId> peers;
    /** log entry old peers */
    private List<PeerId>         oldPeers;
    /** log entry current learners */
    private List<PeerId>         learners;
    /** log entry old learners */
    private List<PeerId>         oldLearners;
    /** entry data */
    private ByteBuffer data;
    private String dataString;
    /** checksum for log entry*/
    private long                 checksum;
    /** true when the log has checksum **/
    private boolean              hasChecksum;

    public EnumOutter.EntryType getType() {
        return type;
    }

    public void setType(EnumOutter.EntryType type) {
        this.type = type;
    }

    public String getDataString() {
        return dataString;
    }

    public void setDataString(String dataString) {
        this.dataString = dataString;
    }

    public LogId getId() {
        return id;
    }

    public void setId(LogId id) {
        this.id = id;
    }

    public List<PeerId> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerId> peers) {
        this.peers = peers;
    }

    public List<PeerId> getOldPeers() {
        return oldPeers;
    }

    public void setOldPeers(List<PeerId> oldPeers) {
        this.oldPeers = oldPeers;
    }

    public List<PeerId> getLearners() {
        return learners;
    }

    public void setLearners(List<PeerId> learners) {
        this.learners = learners;
    }

    public List<PeerId> getOldLearners() {
        return oldLearners;
    }

    public void setOldLearners(List<PeerId> oldLearners) {
        this.oldLearners = oldLearners;
    }

    public ByteBuffer getData() {
        return   ByteBuffer.wrap(getDataString().getBytes());

    }

    public void setData(ByteBuffer data) {

        setDataString(
        ZeroByteStringHelper.byteBufferToString(data));
    }

    public long getChecksum() {
        return checksum;
    }

    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    public boolean isHasChecksum() {
        return hasChecksum;
    }

    public void setHasChecksum(boolean hasChecksum) {
        this.hasChecksum = hasChecksum;
    }

    @Override
    public long checksum() {
        long c = checksum(this.type.getNumber(), this.id.checksum());
        c = checksumPeers(this.peers, c);
        c = checksumPeers(this.oldPeers, c);
        c = checksumPeers(this.learners, c);
        c = checksumPeers(this.oldLearners, c);
        if (this.data != null && this.data.hasRemaining()) {
            c = checksum(c, CrcUtil.crc64(this.data));
        }
        return c;
    }
    private long checksumPeers(final Collection<PeerId> peers, long c) {
        if (peers != null && !peers.isEmpty()) {
            for (final PeerId peer : peers) {
                c = checksum(c, peer.checksum());
            }
        }
        return c;
    }

    public PeerId getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(PeerId leaderId) {
        this.leaderId = leaderId;
    }
}

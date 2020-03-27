package entity;

import java.io.Serializable;

/**
 * Created by 周思成 on  2020/3/11 14:20
 */

/**
 * A raft node identifier.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-03 4:08:14 PM
 */
public final class NodeId implements Serializable {

    private static final long serialVersionUID = 4428173460056804264L;

    /** Raft group id*/
    private final String      groupId;
    /** Node peer id*/
    private final PeerId      peerId;
    /** cached toString result*/
    private String            str;

    public NodeId(String groupId, PeerId peerId) {
        super();
        this.groupId = groupId;
        this.peerId = peerId;
    }

    public String getGroupId() {
        return this.groupId;
    }

    @Override
    public String toString() {
        if (str == null) {
            str = "<" + this.groupId + "/" + this.peerId + ">";
        }
        return str;
    }

    public PeerId getPeerId() {
        return this.peerId;
    }



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.groupId == null ? 0 : this.groupId.hashCode());
        result = prime * result + (this.peerId == null ? 0 : this.peerId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NodeId other = (NodeId) obj;
        if (this.groupId == null) {
            if (other.groupId != null) {
                return false;
            }
        } else if (!this.groupId.equals(other.groupId)) {
            return false;
        }
        if (this.peerId == null) {
            return other.peerId == null;
        } else {
            return this.peerId.equals(other.peerId);
        }
    }
}

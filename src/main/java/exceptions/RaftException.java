package exceptions;

import entity.Status;
import rpc.EnumOutter;

/**
 * Created by 周思成 on  2020/3/11 16:36
 */

public class RaftException extends Throwable{

    private static final long    serialVersionUID = -1533343555230409704L;
    /** Error status*/
    private Status               status           = Status.OK();
    /**
     * Error type
     */
    private EnumOutter.ErrorType type;
    public RaftException(Status status) {
        this.status = status;
    }
    public RaftException(EnumOutter.ErrorType type) {
        super(type.name());
        this.type = type;
    }

    public RaftException() {

    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "RaftException{" +
                "status=" + status +
                '}';
    }
}

package entity;

/**
 * Created by 周思成 on  2020/4/24 16:48
 */

public class RpcResult {

    boolean success = false;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static RpcResult newRpcResult() {
        return new RpcResult();
    }
}

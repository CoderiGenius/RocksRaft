package exceptions;

/**
 * Created by 周思成 on  2020/3/24 16:19
 */

public class ElectionException extends Throwable {

    private String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}

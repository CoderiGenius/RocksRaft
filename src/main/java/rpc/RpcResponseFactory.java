package rpc;

/**
 * Created by 周思成 on  2020/3/13 11:49
 */

import entity.RaftError;
import entity.Status;

/**
 * Helper to create error response.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Mar-28 4:33:50 PM
 */
public class RpcResponseFactory {

    /**
     * Creates a RPC response from status,return OK response
     * when status is null.
     *
     * @param st status with response
     * @return a response instance
     */
    public static RpcRequests.ErrorResponse newResponse(Status st) {
        if (st == null) {
            return newResponse(0, "OK");
        }
        return newResponse(st.getCode(), st.getErrorMsg());
    }

    /**
     * Creates an error response with parameters.
     *
     * @param error error with raft info
     * @param fmt   message with format string
     * @param args  arguments referenced by the format specifiers in the format
     *              string
     * @return a response instance
     */
    public static RpcRequests.ErrorResponse newResponse(RaftError error, String fmt, Object... args) {
        return newResponse(error.getNumber(), fmt, args);
    }

    /**
     * Creates an error response with parameters.
     *
     * @param code  error code with raft info
     * @param fmt   message with format string
     * @param args  arguments referenced by the format specifiers in the format
     *              string
     * @return a response instance
     */
    public static RpcRequests.ErrorResponse newResponse(int code, String fmt, Object... args) {
        RpcRequests.ErrorResponse.Builder builder = RpcRequests.ErrorResponse.newBuilder();
        builder.setErrorCode(code);
        if (fmt != null) {
            builder.setErrorMsg(String.format(fmt, args));
        }
        return builder.build();
    }
}

package rpc;

/**
 * Created by 周思成 on  2020/3/13 11:51
 */

import com.google.protobuf.Message;
import entity.Closure;

/**
 * RPC response closure.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-08 5:55:01 PM
 * @param <T>
 */
public interface RpcResponseClosure<T extends Message> extends Closure {

    /**
     * Called by request handler to set response.
     *
     * @param resp rpc response
     */
    void setResponse(T resp);
}

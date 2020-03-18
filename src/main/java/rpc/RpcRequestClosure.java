package rpc;

import com.google.protobuf.Message;
import entity.Closure;
import entity.Status;
import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;

import com.google.protobuf.Message;

/**
 * Created by 周思成 on  2020/3/13 11:45
 */

/**
 * RPC request Closure encapsulates the RPC contexts.
 *
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Mar-28 4:55:24 PM
 */
public class RpcRequestClosure implements Closure {

    private final BizContext   bizContext;
    private final AsyncContext asyncContext;
    private boolean            respond;

    public RpcRequestClosure(BizContext bizContext, AsyncContext asyncContext) {
        super();
        this.bizContext = bizContext;
        this.asyncContext = asyncContext;
        this.respond = false;
    }

    public BizContext getBizContext() {
        return this.bizContext;
    }

    public AsyncContext getAsyncContext() {
        return this.asyncContext;
    }

    public synchronized void sendResponse(Message msg) {
        if (this.respond) {
            return;
        }
        this.asyncContext.sendResponse(msg);
        this.respond = true;
    }

    @Override
    public void run(Status status) {
        sendResponse(RpcResponseFactory.newResponse(status));
    }
}


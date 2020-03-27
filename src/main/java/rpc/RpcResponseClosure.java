package rpc;

/**
 * Created by 周思成 on  2020/3/13 11:51
 */

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import com.google.protobuf.Message;
import entity.Closure;


public class RpcResponseClosure  implements SofaResponseCallback {


    @Override
    public void onAppResponse(Object o, String s, RequestBase requestBase) {

    }

    @Override
    public void onAppException(Throwable throwable, String s, RequestBase requestBase) {

    }

    @Override
    public void onSofaException(SofaRpcException e, String s, RequestBase requestBase) {

    }
}

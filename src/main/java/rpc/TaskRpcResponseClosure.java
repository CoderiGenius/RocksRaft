package rpc;

/**
 * Created by 周思成 on  2020/3/13 11:51
 */

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Mike
 */
public class TaskRpcResponseClosure<T>  implements SofaResponseCallback<T> {

    public static final Logger LOG = LoggerFactory.getLogger(TaskRpcResponseClosure.class);

    @Override
    public void onAppResponse(Object o, String s, RequestBase requestBase) {

        LOG.info("Receive taskRpcResponse response:requestBase: {} requestString: {}",requestBase.toString(),s);
        switch (requestBase.getMethodName()) {
            case "":

        }
    }

    @Override
    public void onAppException(Throwable throwable, String s, RequestBase requestBase) {

    }

    @Override
    public void onSofaException(SofaRpcException e, String s, RequestBase requestBase) {

    }
}

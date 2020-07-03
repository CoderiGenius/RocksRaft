package rpc;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import com.sun.org.apache.xml.internal.utils.StringVector;
import core.NodeImpl;
import core.RaftGroupService;
import entity.ReadTask;
import entity.ReadTaskResponse;
import entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 周思成 on  2020/4/8 17:13
 */

public class TaskServicesImpl implements TaskRpcServices {
    private static final Logger LOG = LoggerFactory.getLogger(TaskServicesImpl.class);

    @Override
    public void init(String protocol,String ip, String serialization,int port) {

    }

    @Override
    public void apply(Task task) {
        try {
            LOG.debug("Receive task:{}", task.getData());
            NodeImpl.getNodeImple().apply(task);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void apply(Task[] tasks) {
        LOG.info("Apply tasks:{} currentThread:{}",tasks.length,Thread.currentThread().getName());
        for (Task task : tasks) {
            LOG.debug("for :{}",Thread.currentThread().getName());
            NodeImpl.getNodeImple().apply(task);
        }
    }

    @Override
    public ReadTaskResponse handleReadIndexRequest(ReadTask readTask) {
        LOG.debug("Receive readTask");
      return   NodeImpl.getNodeImple().addReadTaskEvent(readTask);
    }
    @Override
    public List<ReadTaskResponse> handleReadIndexRequests(List<ReadTask> readTask) {
        List<ReadTaskResponse> readTaskResponses = new ArrayList<>(readTask.size());
        for (ReadTask r :
                readTask) {
         readTaskResponses.add(handleReadIndexRequest(r));
        }
        return readTaskResponses;
    }


}

package rpc;

import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import core.NodeImpl;
import core.RaftGroupService;
import entity.ReadTask;
import entity.ReadTaskResponse;
import entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 周思成 on  2020/4/8 17:13
 */

public class TaskServicesImpl implements TaskRpcServices {
    private static final Logger LOG = LoggerFactory.getLogger(TaskServicesImpl.class);

    @Override
    public void init(String protocol,String ip, String serialization,int port) {
        ConsumerConfig<ClientRpcService> consumerConfig = new ConsumerConfig<ClientRpcService>()
                .setInterfaceId(ClientRpcService.class.getName())
                .setProtocol(protocol)
                .setSerialization(serialization)
                .setConnectTimeout(2000)
                .setReconnectPeriod(1000)
                .setRetries(100)
                .setDirectUrl(protocol+"://"+ip+":"+port);

        NodeImpl.getNodeImple().setClientRpcService(consumerConfig.refer());
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
        for (Task t:tasks
             ) {
            NodeImpl.getNodeImple().apply(t);
        }
    }

    @Override
    public ReadTaskResponse handleReadIndexRequest(ReadTask readTask) {
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

package rpc;

import core.NodeImpl;
import core.RaftGroupService;
import entity.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/4/8 17:13
 */

public class TaskServicesImpl implements TaskRpcServices {
    private static final Logger LOG = LoggerFactory.getLogger(TaskServicesImpl.class);

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
}

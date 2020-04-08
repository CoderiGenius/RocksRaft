package rpc;

import core.NodeImpl;
import entity.Task;

/**
 * Created by 周思成 on  2020/4/8 17:13
 */

public class TaskServicesImpl implements TaskRpcServices {


    @Override
    public void apply(Task task) {
        NodeImpl.getNodeImple().apply(task);
    }

    @Override
    public void apply(Task[] tasks) {
        for (Task t:tasks
             ) {
            NodeImpl.getNodeImple().apply(t);
        }
    }
}

package rpc;

import entity.Task;

/**
 * Created by 周思成 on  2020/4/8 17:12
 */

public interface TaskRpcServices {

    void apply(final Task task);

    void apply(final Task[] tasks);
}

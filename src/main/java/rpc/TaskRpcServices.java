package rpc;

import entity.ReadTask;
import entity.ReadTaskResponse;
import entity.Task;

import java.util.List;

/**
 * Created by 周思成 on  2020/4/8 17:12
 */

public interface TaskRpcServices {

    void init(String protocol,String ip,String serialization,int port);

    void apply(final Task task);

    void apply(final Task[] tasks);

    ReadTaskResponse handleReadIndexRequest(ReadTask readTask);
    List<ReadTaskResponse> handleReadIndexRequests(List<ReadTask> readTask);
}

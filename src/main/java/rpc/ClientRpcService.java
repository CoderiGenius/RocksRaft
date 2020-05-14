package rpc;

import entity.ReadTask;
import entity.Status;

import java.util.List;

/**
 * Created by 周思成 on  2020/5/8 21:09
 */

public interface ClientRpcService {

    void applied(Status status);

    void appliedBatches(List<Status> statusList);

    void readResult(ReadTask readTask);

    void readResults(List<ReadTask> readTaskList);
}

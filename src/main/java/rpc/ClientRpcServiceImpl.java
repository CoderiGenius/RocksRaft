package rpc;

import entity.ReadTask;
import entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by 周思成 on  2020/5/8 21:12
 */

public class ClientRpcServiceImpl implements ClientRpcService {

    public static final Logger LOG = LoggerFactory.getLogger(ClientRpcServiceImpl.class);

    @Override
    public void applied(Status status) {
        LOG.info("Receive status {}",status);
    }

    @Override
    public void appliedBatches(List<Status> statusList) {
        LOG.info("Receive statusList {}",statusList);
    }

    @Override
    public void readResult(ReadTask readTask) {
        LOG.info("Receive ReadTask {}",readTask);

    }

    @Override
    public void readResults(List<ReadTask> readTaskList) {
        LOG.info("Receive readTaskList {}",readTaskList);

    }
}

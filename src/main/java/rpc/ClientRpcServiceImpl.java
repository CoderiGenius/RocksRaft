package rpc;

import entity.ReadTask;
import entity.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 周思成 on  2020/5/8 21:12
 */

public class ClientRpcServiceImpl implements ClientRpcService {

    public static final Logger LOG = LoggerFactory.getLogger(ClientRpcServiceImpl.class);
    public static AtomicInteger atomicInteger = new AtomicInteger(0);
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
        try {
            LOG.info("Receive ReadTask {}", readTask);
            String result = new String(readTask.getTaskBytes(), StandardCharsets.UTF_8);
            LOG.info("Read result:" + result);
            atomicInteger.addAndGet(1);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("readResult error:{}",e.getMessage());
        }

    }

    @Override
    public void readResults(List<ReadTask> readTaskList) {
        LOG.info("Receive readTaskList {}",readTaskList);
        for (ReadTask r :
                readTaskList) {
            readResult(r);
        }
    }
}

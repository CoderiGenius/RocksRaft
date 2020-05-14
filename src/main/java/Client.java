import com.alipay.sofa.rpc.config.ConsumerConfig;
import entity.KVEntity;
import entity.Task;
import rpc.TaskRpcResponseClosure;
import rpc.TaskRpcServices;
import rpc.TaskServicesImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by 周思成 on  2020/4/25 12:28
 */

public class Client {


    public static void main(String[] args) throws IOException, InterruptedException {
        ConsumerConfig<TaskRpcServices> consumerConfigForTasks = new ConsumerConfig<TaskRpcServices>()
                .setInvokeType("callback")
                .setOnReturn(new TaskRpcResponseClosure())
                .setProtocol("bolt")
                .setConnectTimeout(2000)
                .setDirectUrl("bolt"
                        + "://" + "127.0.0.1" + ":" + 12221)
                .setInterfaceId(TaskRpcServices.class.getName());

       TaskRpcServices taskServices = consumerConfigForTasks.refer();
        Task task = new Task();
//        KVEntity kvEntity = new KVEntity("1","123");
//
//
//        ByteArrayOutputStream bout = new ByteArrayOutputStream();
//        ObjectOutputStream out = new ObjectOutputStream(bout);
//        out.writeObject(kvEntity);
//        out.flush();
//        byte[] bytes = bout.toByteArray();
//        bout.close();
//        out.close();

        ByteBuffer byteBuffer =  ByteBuffer.wrap("123".getBytes());

        task.setData(byteBuffer);

        int i = 100;
        while (i>=100) {
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            taskServices.apply(task);
            i--;
        }

       Thread.currentThread().join();
    }
}

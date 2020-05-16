import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import entity.KVEntity;
import entity.Task;
import rpc.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * Created by 周思成 on  2020/4/25 12:28
 */

public class Client {


    public static void main(String[] args) throws IOException, InterruptedException {

        ServerConfig serverConfig = new ServerConfig()
                .setProtocol("bolt") // 设置一个协议，默认bolt

                .setPort(12299) // 设置一个端口，默认12200
                .setDaemon(false); // 非守护线程

        ProviderConfig<ClientRpcService> providerConfig = new ProviderConfig<ClientRpcService>()
                .setInterfaceId(ClientRpcService.class.getName()) // 指定接口
                .setRef(new ClientRpcServiceImpl()) // 指定实现
                .setServer(serverConfig); // 指定服务端

        providerConfig.export(); // 发布服务


        ConsumerConfig<TaskRpcServices> consumerConfigForTasks = new ConsumerConfig<TaskRpcServices>()
                .setInvokeType("callback")
                .setOnReturn(new TaskRpcResponseClosure())
                .setProtocol("bolt")
                .setConnectTimeout(2000)
                .setDirectUrl("bolt"
                        + "://" + "127.0.0.1" + ":" + 12201)
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

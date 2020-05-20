import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import core.CustomStateMachine;
import entity.KVEntity;
import entity.Task;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rpc.*;
import utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.DataFormatException;

/**
 * Created by 周思成 on  2020/4/25 12:28
 */

public class Client {
    private static final Logger LOG = LoggerFactory.getLogger(Client.class);

    public static boolean flag = true;
    public static AtomicInteger atomicIntegerForTansactions = new AtomicInteger(0);
    public static void main(String[] args) throws IOException, InterruptedException {

        ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

        ServerConfig serverConfig = new ServerConfig()
                .setProtocol("bolt")
                .setPort(12299)
                .setDaemon(false);

        ProviderConfig<ClientRpcService> providerConfig = new ProviderConfig<ClientRpcService>()
                .setInterfaceId(ClientRpcService.class.getName())
                .setRef(new ClientRpcServiceImpl())
                .setServer(serverConfig);

        providerConfig.export();


        ConsumerConfig<TaskRpcServices> consumerConfigForTasks = new ConsumerConfig<TaskRpcServices>()
                .setInvokeType("callback")
                .setOnReturn(new TaskRpcResponseClosure())
                .setProtocol("bolt")
                .setConnectTimeout(2000)
                .setDirectUrl("bolt"
                       + "://" + "172.24.234.215" + ":" + 12221)
                        //+ "://" + "localhost" + ":" + 12221)
                .setInterfaceId(TaskRpcServices.class.getName());

        long startTime =   Utils.monotonicMs();


        TaskRpcServices taskServices = consumerConfigForTasks.refer();





        Runnable runnable1 = () -> {
            int i = 0;
            while (flag) {
                Task task = new Task();
                ByteBuffer byteBuffer =  ByteBuffer.wrap(("1log"+i).getBytes());
                task.setData(byteBuffer);
                taskServices.apply(task);
                Client.atomicIntegerForTansactions.addAndGet(1);
                i++;
            }
        };

        Runnable runnable2 = () -> {
            int i = 0;
            while (true) {
                Task task = new Task();
                ByteBuffer byteBuffer =  ByteBuffer.wrap(("2log"+i).getBytes());
                task.setData(byteBuffer);
                taskServices.apply(task);
                Client.atomicIntegerForTansactions.addAndGet(1);
                i++;
            }
        };

        Runnable runnable3 = () -> {
            int i = 0;
            while (flag) {
                Task[] task = new Task[200];
                for (int j = 0; j <200 ; j++) {
                    Task task1 = new Task();
                    ByteBuffer byteBuffer =  ByteBuffer.wrap(("3log"+i).getBytes());
                    task1.setData(byteBuffer);
                    task[j] = task1;
                }
                taskServices.apply(task);
                Client.atomicIntegerForTansactions.addAndGet(task.length);
                i++;
            }
        };
        Runnable runnable4 = () -> {
            int i = 0;
            while (true) {
                Task[] task = new Task[10];
                for (int j = 0; j <10 ; j++) {
                    Task task1 = new Task();
                    ByteBuffer byteBuffer =  ByteBuffer.wrap(("4log"+i).getBytes());
                    task1.setData(byteBuffer);
                    task[i] = task1;
                }

                taskServices.apply(task);
                Client.atomicIntegerForTansactions.addAndGet(task.length);
                i++;
            }
        };
//        cachedThreadPool.submit(runnable1);
//        cachedThreadPool.submit(runnable2);
        cachedThreadPool.execute(runnable3);
        //runnable1.run();
        //cachedThreadPool.submit(runnable3);
        Thread.sleep(1000);
        System.exit(0);
//        cachedThreadPool.shutdown();
//        flag=false;
//        long totalTime = (Utils.monotonicMs()-startTime);
//        System.out.println("***************** Raft Test Result ******************");
//        System.out.println("Total Transaction:           "+atomicIntegerForTansactions.get());
//        System.out.println("Total Running Time:          "+totalTime);
//        System.out.println("Total Transaction Processed: "+ClientRpcServiceImpl.atomicInteger);
//        System.out.println("Average Transaction Time:    "+(totalTime/ClientRpcServiceImpl.atomicInteger.get()));
//        System.out.println("****************************************************");
//       Thread.currentThread().join();
    }
}

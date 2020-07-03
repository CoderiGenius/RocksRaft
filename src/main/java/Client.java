import com.alipay.sofa.rpc.config.ConsumerConfig;
import com.alipay.sofa.rpc.config.ProviderConfig;
import com.alipay.sofa.rpc.config.ServerConfig;
import core.CustomStateMachine;
import entity.KVEntity;
import entity.ReadTask;
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
                       //+ "://" + "172.24.234.215" + ":" + 12221)
                        + "://" + "localhost" + ":" + 12221)
                .setInterfaceId(TaskRpcServices.class.getName());

        long startTime =   Utils.monotonicMs();


        TaskRpcServices taskServices = consumerConfigForTasks.refer();





        
    }
}

import org.junit.Test;
import utils.TimerManager;

import java.util.concurrent.TimeUnit;

/**
 * Created by 周思成 on  2020/4/23 18:41
 */

public class TestTimeManager {

    TimerManager timerManager = new TimerManager();
    @Test
    public void Test() throws InterruptedException {
        Runnable runnable = () -> {
            System.out.println(123);
            try {
                Test();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //timerManager.schedule(runnable,1000, TimeUnit.MILLISECONDS);
        }
        ;


        timerManager.init(100);
        timerManager.schedule(runnable,1000, TimeUnit.MILLISECONDS);
        Thread.currentThread().join();
    }
}

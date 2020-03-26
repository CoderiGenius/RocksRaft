package core;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by 周思成 on  2020/3/24 15:31
 */

@Deprecated
public class HeartbeatTimer extends TimerTask {

    private String name;
    public HeartbeatTimer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {

    }
}

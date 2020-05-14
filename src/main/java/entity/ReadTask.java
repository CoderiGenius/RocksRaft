package entity;

import java.io.Serializable;

/**
 * Created by 周思成 on  2020/5/8 12:39
 */

public class ReadTask implements Serializable {

    private static final long serialVersionUID = -4427655712599189827L;


    private byte[] taskBytes;

    public byte[] getTaskBytes() {
        return taskBytes;
    }

    public void setTaskBytes(byte[] taskBytes) {
        this.taskBytes = taskBytes;
    }

    public ReadTask(byte[] taskBytes) {
        this.taskBytes = taskBytes;
    }

}

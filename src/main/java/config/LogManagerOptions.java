package config;

import entity.LogManager;
import entity.Options;
import storage.LogStorage;

/**
 * Created by 周思成 on  2020/4/7 20:28
 */

public class LogManagerOptions {

    private LogStorage logStorage;
    private Options options;
    private int                  disruptorBufferSize  = 1024;


    public int getDisruptorBufferSize() {
        return disruptorBufferSize;
    }

    public void setDisruptorBufferSize(int disruptorBufferSize) {
        this.disruptorBufferSize = disruptorBufferSize;
    }

    public void setLogStorage(LogStorage logStorage) {
        this.logStorage = logStorage;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    public LogStorage getLogStorage() {
        return this.logStorage;
    }
}

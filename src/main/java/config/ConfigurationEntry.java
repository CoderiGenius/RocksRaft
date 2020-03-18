package config;

import entity.LogId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by 周思成 on  2020/3/13 16:51
 */

public class ConfigurationEntry {


    private static final Logger LOG     = LoggerFactory.getLogger(ConfigurationEntry.class);

    private LogId id      = new LogId(0, 0);
    private Configuration       conf    = new Configuration();
    private Configuration       oldConf = new Configuration();


    public static Logger getLOG() {
        return LOG;
    }

    public LogId getId() {
        return id;
    }

    public void setId(LogId id) {
        this.id = id;
    }

    public Configuration getConf() {
        return conf;
    }

    public void setConf(Configuration conf) {
        this.conf = conf;
    }

    public Configuration getOldConf() {
        return oldConf;
    }

    public void setOldConf(Configuration oldConf) {
        this.oldConf = oldConf;
    }
}

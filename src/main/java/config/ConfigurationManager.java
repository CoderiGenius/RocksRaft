package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Created by 周思成 on  2020/3/13 16:51
 */

public class ConfigurationManager {

    private static final Logger LOG            = LoggerFactory.getLogger(ConfigurationManager.class);

    private final LinkedList<ConfigurationEntry> configurations = new LinkedList<>();
    private ConfigurationEntry                   snapshot       = new ConfigurationEntry();

    /**
     * Adds a new conf entry.
     */
    public boolean add(final ConfigurationEntry entry) {
        if (!this.configurations.isEmpty()) {
            if (this.configurations.peekLast().getId().getIndex() >= entry.getId().getIndex()) {
                LOG.error("Did you forget to call truncateSuffix before the last log index goes back.");
                return false;
            }
        }
        return this.configurations.add(entry);
    }


    public static Logger getLOG() {
        return LOG;
    }

    public LinkedList<ConfigurationEntry> getConfigurations() {
        return configurations;
    }

    public ConfigurationEntry getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(ConfigurationEntry snapshot) {
        this.snapshot = snapshot;
    }
}

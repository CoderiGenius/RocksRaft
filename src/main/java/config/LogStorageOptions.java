package config;

/**
 * Created by 周思成 on  2020/3/13 16:50
 */

import entity.LogEntryCodecFactory;

/**
 * Log storage initialize options
 * @author boyan(boyan@antfin.com)
 *
 */
public class LogStorageOptions {

    private ConfigurationManager configurationManager;

    private LogEntryCodecFactory logEntryCodecFactory;

    public ConfigurationManager getConfigurationManager() {
        return this.configurationManager;
    }

    public void setConfigurationManager(final ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    public LogEntryCodecFactory getLogEntryCodecFactory() {
        return this.logEntryCodecFactory;
    }

    public void setLogEntryCodecFactory(final LogEntryCodecFactory logEntryCodecFactory) {
        this.logEntryCodecFactory = logEntryCodecFactory;
    }



}

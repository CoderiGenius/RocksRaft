package entity;

/**
 * Created by 周思成 on  2020/4/14 16:27
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * A configuration entry with current peers and old peers.
 * @author boyan (boyan@alibaba-inc.com)
 *
 * 2018-Apr-04 2:25:06 PM
 */
public class ConfigurationEntry {

    private static final Logger LOG     = LoggerFactory.getLogger(ConfigurationEntry.class);

    private LogId               id      = new LogId(0, 0);


    public LogId getId() {
        return this.id;
    }

    public void setId(final LogId id) {
        this.id = id;
    }





}
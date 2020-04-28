package entity;

import java.io.Serializable;

/**
 * Created by 周思成 on  2020/4/25 12:48
 */

public class KVEntity implements Serializable {

    private static final long serialVersionUID = -7329681263115546100L;

    String value;
    String key;

    public KVEntity(String value, String key) {
        this.value = value;
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}

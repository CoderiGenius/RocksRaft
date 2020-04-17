package entity;

/**
 * Created by 周思成 on  2020/4/17 12:39
 */

public class RocksBatch {


   private byte[] key;
   private byte[] value;

    public RocksBatch(byte[] key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public byte[] getKey() {
        return key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
}

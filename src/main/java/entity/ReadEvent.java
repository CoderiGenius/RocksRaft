package entity;

/**
 * Created by 周思成 on  2020/4/9 20:40
 */

public class ReadEvent {

   public byte[] entry;
    public  long expectedIndex;
    public Closure        done;

    public void reset(){
        this.entry = null;
        this.expectedIndex = 0;
        this.done = null;
    }

    public ReadEvent(byte[] entry, long expectedIndex, Closure done) {
        this.entry = entry;
        this.expectedIndex = expectedIndex;
        this.done = done;
    }
    public ReadEvent() {

    }
}

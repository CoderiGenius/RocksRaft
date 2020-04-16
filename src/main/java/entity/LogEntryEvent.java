package entity;

/**
 * Created by 周思成 on  2020/4/9 20:40
 */

public class LogEntryEvent {

   public LogEntry entry;
    public  long expectedTerm;
    public Closure        done;

    public void reset(){
        this.entry = null;
        this.expectedTerm = 0;
        this.done = null;
    }

    public LogEntryEvent(LogEntry entry, long expectedTerm, Closure done) {
        this.entry = entry;
        this.expectedTerm = expectedTerm;
        this.done = done;
    }
    public LogEntryEvent() {

    }
}

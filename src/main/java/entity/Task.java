package entity;

import sun.plugin2.message.Serializer;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Created by 周思成 on  2020/4/8 16:41
 */

public class Task implements Serializable {


    private static final long serialVersionUID = -4427655712599189826L;
    private ByteBuffer data;

    private Closure done;

    private long expectedTerm = -1;


    /**
     * Creates a task with data/done.
     */
    public Task(ByteBuffer data, Closure done) {
        super();
        this.data = data;
        this.done = done;
    }

    public Task(ByteBuffer data, Closure done, long expectedTerm) {
        super();
        this.data = data;
        this.done = done;
        this.expectedTerm = expectedTerm;
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public Closure getDone() {
        return done;
    }

    public void setDone(Closure done) {
        this.done = done;
    }

    public long getExpectedTerm() {
        return expectedTerm;
    }

    public void setExpectedTerm(long expectedTerm) {
        this.expectedTerm = expectedTerm;
    }
}

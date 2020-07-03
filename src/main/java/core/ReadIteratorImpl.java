package core;

import entity.Closure;
import entity.Iterator;
import entity.ReadEvent;
import entity.Status;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by 周思成 on  2020/5/15 15:12
 */

public class ReadIteratorImpl implements Iterator {

    byte[] data;
    private final String OPERATION = "READ";
    int size;
    int currentIndex;
    List<ReadEvent> list;

    public ReadIteratorImpl(List<ReadEvent> list) {
        this.list = list;
        this.size = list.size();
        this.currentIndex = 0;
        this.data = list.get(0).entry;
    }

    @Override
    public String getOperation() {
        return OPERATION;
    }

    @Override
    public ByteBuffer getData() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data);
        return byteBuffer;
    }

    @Override
    public long getIndex() {
        return currentIndex;
    }

    @Override
    public long getTerm() {
        return 0;
    }

    @Override
    public Closure done() {
        return null;
    }

    @Override
    public void setErrorAndRollback(long ntail, Status st) {

    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public boolean hasNext() {
        return (currentIndex+1)<(size);
    }

    @Override
    public ByteBuffer next() {
            this.currentIndex = currentIndex +1;

            this.data = list.get(currentIndex).entry;
        return getData();
    }


}

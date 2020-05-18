package core;

import entity.Closure;
import entity.Iterator;
import entity.LogEntry;
import entity.Status;
import rpc.EnumOutter;

import java.nio.ByteBuffer;

/**
 * Created by 周思成 on  2020/4/22 13:54
 */

public class IteratorWrapper implements Iterator {

    private final IteratorImpl impl;

    private final String OPERATION = "LOG";


    public IteratorWrapper(IteratorImpl iterImpl) {
        super();
        this.impl = iterImpl;
    }

    @Override
    public boolean hasNext() {
        return this.impl.isGood() ;
    }

    @Override
    public ByteBuffer next() {
        final ByteBuffer data = getData();
        if (hasNext()) {
            this.impl.next();
        }
        return data;
    }

    @Override
    public String getOperation() {
        return getOPERATION();
    }

    @Override
    public ByteBuffer getData() {
        final LogEntry entry = this.impl.entry();
        return entry != null ? entry.getData() : null;
    }

    @Override
    public long getIndex() {
        return this.impl.getIndex();
    }

    @Override
    public long getTerm() {
        return this.impl.entry().getId().getTerm();
    }

    @Override
    public Closure done() {
        //return this.impl.done();
        return null;
    }

    @Override
    public void setErrorAndRollback(final long ntail, final Status st) {

        //this.impl.setErrorAndRollback(ntail, st);
    }

    @Override
    public int getSize() {
        return 0;
    }

    public IteratorImpl getImpl() {
        return impl;
    }

    public String getOPERATION() {
        return OPERATION;
    }
}

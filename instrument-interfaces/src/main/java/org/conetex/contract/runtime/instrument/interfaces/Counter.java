package org.conetex.contract.runtime.instrument.interfaces;

public interface Counter {

    public Counter getPrevious();

    public long getCount();

}

package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.Counter;

public final class DefaultCounter implements Counter{

    final Counter previousCounter;

    long value;

    DefaultCounter(Counter previous, Config config){
        this.previousCounter = previous;
        this.value = config.min;
    }

    public final long getValue() {
        return this.value;
    }

    public final Counter getPrevious() {
        return this.previousCounter;
    }

    public final boolean hasPrevious() {
        return true;
    }



}


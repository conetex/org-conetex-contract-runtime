package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.Counter;

public class NullCounter extends AbstractCounter {

    private final long value;

    NullCounter(Stack parent){
        this.value = parent.getCounterMin();
    }

    public final long getValue() {
        return this.value;
    }

    // todo is null ok here? thow exception? return this?
    public final Counter getPrevious() {
        return null;
    }

    public final boolean hasPrevious() {
        return false;
    }

}


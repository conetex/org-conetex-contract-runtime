package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.counter.LinkedLong;

final class Tail implements LinkedLong {

    private final long value;

    Tail(LongLimits c){
        this.value = c.min();
    }

    public long getValue() {
        return this.value;
    }

    // todo is null ok here? thow exception? return this?
    public LinkedLong getPrevious() {
        return null;
    }

    public boolean hasPrevious() {
        return false;
    }

}


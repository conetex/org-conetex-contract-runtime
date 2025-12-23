package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.counter.LinkedLong;

final class Node implements LinkedLong {

    final LinkedLong previousCounter;

    long value;

    Node(LinkedLong previous, LongLimits config){
        this.previousCounter = previous;
        this.value = config.min();
    }

    public long getValue() {
        return this.value;
    }

    public LinkedLong getPrevious() {
        return this.previousCounter;
    }

    public boolean hasPrevious() {
        return true;
    }

}


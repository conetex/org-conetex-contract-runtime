package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.Counter;

public abstract class AbstractCounter implements Counter {

    AbstractCounter(){}

    public final DefaultCounter createNext() {
        return new DefaultCounter(this);
    }

}


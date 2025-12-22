package org.conetex.runtime.instrument.interfaces;

public interface Configuration extends MathWithMinLongMaxLongConfiguration {
    Counter[] countPreviousOnAll(Counter[] counters);

    boolean containsCountableCounters(Counter[] counters);
}

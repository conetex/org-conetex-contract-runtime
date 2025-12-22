package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.Configuration;
import org.conetex.runtime.instrument.interfaces.Counter;

public class Config implements Configuration {

    final long min;
    @Override
    public long min() {
        return this.min;
    }

    final long max;
    @Override
    public long max() {
        return this.max;
    }

    public Config(long min, long max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public boolean containsCountableCounters(Counter[] counters) {
        for (Counter counter : counters) {
            if (counter.getValue() > this.min) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized Counter[] countPreviousOnAll(Counter[] counters) {
        Counter[] counterCounter = new Counter[counters.length];

        for (int i = 0; i < counters.length; i++) {

            if(counters[i].hasPrevious()){
                counterCounter[i] = this.countPrevious(counters[i].getPrevious());
            }
            else{
                counterCounter[i] = new NullCounter(this);
            }

        }

        return counterCounter;
    }

    private Counter countPrevious(Counter current) {
        if(current.hasPrevious()){
            DefaultCounter previousCounter = new DefaultCounter(new NullCounter(this), this);
            do {

                if (previousCounter.value == this.max) {
                    previousCounter = new DefaultCounter(previousCounter, this);
                }
                previousCounter.value++;

                current = current.getPrevious();
            } while (current.hasPrevious());
            return previousCounter;
        }
        else{
            return new NullCounter(this);
        }
    }


}

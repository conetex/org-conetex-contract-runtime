package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.Counter;

public class DefaultCounter extends AbstractCounter {

    protected final AbstractCounter previousCounter;

    protected long value = Stack.COUNTER_MIN_VALUE;

    DefaultCounter(AbstractCounter previous){
        this.previousCounter = previous;
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

    public static synchronized boolean containsCountableCounters(Stack[] stacks, Counter[] counters) {
        for (int i = 0; i < counters.length; i++) {
            if (counters[i].getValue() > stacks[i].getCounterMin()) {
                return true;
            }
        }
        return false;
    }

    public static synchronized AbstractCounter[] countPreviousOnAll(Stack[] stacks, Counter[] counters) {
        AbstractCounter[] counterCounter = new AbstractCounter[counters.length];

        for (int i = 0; i < counters.length; i++) {

            if(counters[i].hasPrevious()){
                counterCounter[i] = countPrevious(stacks[i], counters[i].getPrevious());
            }
            else{
                counterCounter[i] = new NullCounter(stacks[i]);
            }

        }

        return counterCounter;
    }

    private static AbstractCounter countPrevious(Stack stack, Counter current) {
        if(current.hasPrevious()){
            DefaultCounter previousCounter = new DefaultCounter(new NullCounter(stack));
            do {

                if (previousCounter.value == Stack.COUNTER_MAX_VALUE) {
                    previousCounter = new DefaultCounter(previousCounter);
                }
                previousCounter.value++;

                current = current.getPrevious();
            } while (current.hasPrevious());
            return previousCounter;
        }
        else{
            return new NullCounter(stack);
        }

    }

}


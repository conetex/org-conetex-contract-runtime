package org.conetex.runtime.instrument.counter;

public class Stack {

    public final static long COUNTER_MIN_VALUE = 0;//Long.MIN_VALUE;

    public final static long COUNTER_MAX_VALUE = 1001;//Long.MAX_VALUE;

    private DefaultCounter top = (new NullCounter(this)).createNext();

    public final synchronized DefaultCounter peek() {
        return this.top;
    }

    private boolean isInProgress = false;

    public long getCounterMin() {
        return counterMin;
    }

    final long counterMin;

    public Stack(long counterMin){
        this.counterMin = counterMin;
    }

    public final synchronized void reset() {
        this.top = (new NullCounter(this)).createNext();
    }

    /**
     * Increments the counter. This method is designed to track and count costs in the
     * program by increasing the counter value in a thread-safe manner. If the current counter
     * reaches its maximum value, a new counter instance is created and linked to the previous one.
     *
     * Key implementation details:
     * - This method does not use or depend on any external classes or methods outside this class,
     *   except for Java primitives and `synchronized`, which are part of the core language.
     * - All operations are performed only with primitive types (e.g., `long`, `boolean`) or
     *   internal class references (e.g., `head`, `previousCounter`), ensuring independence from
     *   any potentially instrumented classes.
     * - A safeguard (`isInProgress`) is implemented to detect and prevent recursive calls,
     *   which avoids endless recursion and ensures the integrity of the counter state.
     *
     * As a result, this method is safe to use in instrumented environments, as it avoids any
     * circular calls or interference caused by instrumented classes or dependencies.
     */
    public final synchronized void increment() {
        if (this.isInProgress) {
            // We are already inside increment() → endless recursion detected
            return;
        }
        this.isInProgress = true;
        try {
            if (this.top.value == Stack.COUNTER_MAX_VALUE) {
                this.top = this.top.createNext();
            }
            this.top.value++;
        } finally {
            this.isInProgress = false;
        }
    }

}


package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.counter.ChainOfLongs;
import org.conetex.runtime.instrument.interfaces.counter.LinkedLong;

public class Counter implements ChainOfLongs {

    private Node top;

    public final synchronized LinkedLong peek() {
        return this.top;
    }

    private boolean isInProgress = false;

    final LongLimits minMax;

    public Counter(LongLimits config){
        this.minMax = config;
        this.top = new Node(new Tail(this.minMax), this.minMax);
    }

    public final synchronized void reset() {
        this.top = new Node(new Tail(this.minMax), this.minMax);
    }

    /**
     * Increments the counter. This method is designed to track and count costs in the
     * program by increasing the counter value in a thread-safe manner. If the current counter
     * reaches its maximum value, a new counter instance is created and linked to the previous one.
     * Key implementation details:
     * - This method does not use or depend on any external classes or methods outside this class,
     *   except for Java primitives and `synchronized`, which are part of the core language.
     * - All operations are performed only with primitive types (e.g., `long`, `boolean`) or
     *   internal class references (e.g., `head`, `previousCounter`), ensuring independence from
     *   any potentially instrumented classes.
     * - A safeguard (`isInProgress`) is implemented to detect and prevent recursive calls,
     *   which avoids endless recursion and ensures the integrity of the counter state.
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
            if (this.top.value == this.minMax.max()) {
                this.top = new Node(this.top, this.minMax);
            }
            this.top.value++;
        } finally {
            this.isInProgress = false;
        }
    }














}


package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.counter.Config;
import org.conetex.runtime.instrument.counter.Stack;

public final class Counters {

    public final static Config NEW_MIN_VALUE = new Config(0L, 10L);

    public static final Stack ARITHMETIC_ADD_SUB_NEG = new Stack(NEW_MIN_VALUE);
    public static final Stack ARITHMETIC_DIV_REM = new Stack(NEW_MIN_VALUE);
    public static final Stack ARITHMETIC_MUL = new Stack(NEW_MIN_VALUE);
    public static final Stack ARRAY_LOAD = new Stack(NEW_MIN_VALUE);
    public static final Stack ARRAY_NEW = new Stack(NEW_MIN_VALUE);
    public static final Stack ARRAY_STORE = new Stack(NEW_MIN_VALUE);
    public static final Stack COMPARE_INT = new Stack(NEW_MIN_VALUE);
    public static final Stack COMPARE_LONG = new Stack(NEW_MIN_VALUE);
    public static final Stack COMPARE_OBJECT = new Stack(NEW_MIN_VALUE);
    public static final Stack EXCEPTION_THROW = new Stack(NEW_MIN_VALUE);
    public static final Stack FIELD_LOAD = new Stack(NEW_MIN_VALUE);
    public static final Stack FIELD_STORE = new Stack(NEW_MIN_VALUE);
    public static final Stack JUMP = new Stack(NEW_MIN_VALUE);
    public static final Stack METHOD_CALL = new Stack(NEW_MIN_VALUE);
    public static final Stack METHOD_ENTRY = new Stack(NEW_MIN_VALUE);
    public static final Stack MONITOR = new Stack(NEW_MIN_VALUE);
    public static final Stack VARIABLE_LOAD = new Stack(NEW_MIN_VALUE);
    public static final Stack VARIABLE_STORE = new Stack(NEW_MIN_VALUE);
    public static final Stack TYPE_CHECK = new Stack(NEW_MIN_VALUE);

    public static String echo(String in) {
        return in;
    }

    public static void incrementArithmeticAddSubNeg() {
        Counters.ARITHMETIC_ADD_SUB_NEG.increment();
    }

    public static void incrementArithmeticDivRem() {
        Counters.ARITHMETIC_DIV_REM.increment();
    }

    public static void incrementArithmeticMul() {
        Counters.ARITHMETIC_MUL.increment();
    }

    public static void incrementArrayLoad() {
        Counters.ARRAY_LOAD.increment();
    }

    public static void incrementArrayNew() {
        Counters.ARRAY_NEW.increment();
    }

    public static void incrementArrayStore() {
        Counters.ARRAY_STORE.increment();
    }

    public static void incrementCompareInt() {
        Counters.COMPARE_INT.increment();
    }

    public static void incrementCompareLong() {
        Counters.COMPARE_LONG.increment();
    }

    public static void incrementCompareObject() {
        Counters.COMPARE_OBJECT.increment();
    }

    public static void incrementExceptionThrow() {
        Counters.EXCEPTION_THROW.increment();
    }

    public static void incrementFieldLoad() {
        Counters.FIELD_LOAD.increment();
    }

    public static void incrementFieldStore() {
        Counters.FIELD_STORE.increment();
    }

    public static void incrementJump() {
        Counters.JUMP.increment();
    }

    public static void incrementMethodCall() {
        Counters.METHOD_CALL.increment();
    }

    public static void incrementMethodEntry() {
        Counters.METHOD_ENTRY.increment();
    }

    public static void incrementMonitor() {
        Counters.MONITOR.increment();
    }

    public static void incrementVariableLoad() {
        Counters.VARIABLE_LOAD.increment();
    }

    public static void incrementVariableStore() {
        Counters.VARIABLE_STORE.increment();
    }

    public static void incrementTypeCheck() {
        Counters.TYPE_CHECK.increment();
    }

}

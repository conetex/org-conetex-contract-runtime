package org.conetex.runtime.instrument.test.jar;

import org.conetex.runtime.instrument.counter.Counter;
import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.interfaces.arithmetic.ChainsOfLongs;
import org.conetex.runtime.instrument.interfaces.counter.Incrementable;
import org.conetex.runtime.instrument.metrics.cost.Counters;

public class Main {

    public static final String TEST_FAILED = "test FAILED";
    public static final String TEST_OK = "test OK";

    public static void main(String[] args) {

        warmup();

        // test
        testsIncrementableInterfaceDefault();
        testsIncrementableInterfaceBlock();

        testsIncrementableCounterDefault();
        testsIncrementableCounterBlock();

        testsChainsOfLongsCountersWeighted();
        testsChainsOfLongsInterface();

        testsCountersReset();
        testsCountersBlockIncrement();
    }

    static void warmup() {
        // warmup 1
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Incrementable c = Counters.COUNTERS[i];
            c.reset();
            Counters.COUNTERS[i].reset();
            long counterBefore = c.peek().getValue();
            c.increment();
            long counterAfter = c.peek().getValue();
            //outputResult(counterAfter, counterBefore, expected, testName, i);
        }

        // warmup 2
        Counters.CONFIG.average();
    }

    static String testsIncrementableCounterDefault() {
        String testName = "Counter (Incrementable) | default increment";
        long expected = 1;

        StringBuilder msg = new StringBuilder();
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Counter c = Counters.COUNTERS[i];
            c.reset();
            long counterBefore = c.peek().getValue();
            c.increment();
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsIncrementableCounterBlock() {
        String testName = "Counter (Incrementable) | blocked increment";
        long expected = 0;

        StringBuilder msg = new StringBuilder();
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Counter c = Counters.COUNTERS[i];
            c.reset();
            long counterBefore = c.peek().getValue();
            c.blockIncrement(true);
            c.increment();
            c.blockIncrement(false);
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsIncrementableInterfaceDefault() {
        String testName = "Incrementable | default increment";
        long expected = 1;

        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Incrementable c = Counters.COUNTERS[i];
            c.reset();
            Counters.COUNTERS[i].reset();
            long counterBefore = c.peek().getValue();
            c.increment();
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsIncrementableInterfaceBlock() {
        String testName = "Incrementable | blocked increment";
        long expected = 0;

        StringBuilder msg = new StringBuilder();
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Incrementable c = Counters.COUNTERS[i];
            c.reset();
            long counterBefore = c.peek().getValue();
            c.blockIncrement(true);
            c.increment();
            c.blockIncrement(false);
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsChainsOfLongsCountersWeighted() {
        String testName = "CountersWeighted (ChainsOfLongs) | average";
        long expected = 0;

        StringBuilder msg = new StringBuilder();
        CountersWeighted testObject = Counters.CONFIG;
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Counter c = Counters.COUNTERS[i];

            long counterBefore = c.peek().getValue();
            Counters.CONFIG.average();
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsChainsOfLongsInterface() {
        String testName = "CountersWeighted (ChainsOfLongs) | average";
        long expected = 0;

        StringBuilder msg = new StringBuilder();
        ChainsOfLongs testObject = Counters.CONFIG;
        for (int i = 0; i < Counters.COUNTERS.length; i++) {
            Counter c = Counters.COUNTERS[i];

            long counterBefore = c.peek().getValue();
            testObject.average();
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsCountersReset() {
        String testName = "Counters | reset";
        long expected = 0;

        StringBuilder msg = new StringBuilder();
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            Counter c = Counters.COUNTERS[i];
            Counters.reset();
            long counterBefore = c.peek().getValue();
            Counters.reset();
            long counterAfter = c.peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    static String testsCountersBlockIncrement() {
        String testName = "Counters | blockIncrement";
        long expected = 0;

        StringBuilder msg = new StringBuilder();
        for(int i = 0; i < Counters.COUNTERS.length; i++){
            long counterBefore = Counters.COUNTERS[i].peek().getValue();
            Counters.blockIncrement(true);
            Counters.blockIncrement(true);
            Counters.blockIncrement(false);
            Counters.blockIncrement(false);
            Counters.blockIncrement(true);
            Counters.blockIncrement(true);
            Counters.blockIncrement(false);
            Counters.blockIncrement(false);
            long counterAfter = Counters.COUNTERS[i].peek().getValue();

            msg.append(outputResult(counterAfter, counterBefore, expected, testName, i));
        }
        return msg.toString();
    }

    private static String outputResult(long counterAfter, long counterBefore, long expected, String testName, int i) {
        long counterDiff = (counterAfter - counterBefore);
        String msg = ": '" + testName + "', on: '" + i + "', expected: '" + expected + "', actual: " + counterAfter + " - " + counterBefore + " = " + counterDiff + System.lineSeparator();
        if (counterDiff != expected) {
            msg = TEST_FAILED + msg;
            System.err.print(msg); // ok
        } else {
            msg = TEST_OK     + msg;
            System.out.print(msg); // ok
        }
        return msg;
    }

}
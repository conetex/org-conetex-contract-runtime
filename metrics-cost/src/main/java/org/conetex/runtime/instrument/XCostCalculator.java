package org.conetex.runtime.instrument;

import static org.conetex.runtime.instrument.metrics.cost.Counters.*;

// todo klasse wird nicht genutzt
public class XCostCalculator {

    // Gewichte für die einzelnen Counter
    private static final int WEIGHT_METHOD_CALL = 5;
    private static final int WEIGHT_METHOD_ENTRY = 5;
    private static final int WEIGHT_JUMP = 2;
    private static final int WEIGHT_COMPARE_INT = 1;
    private static final int WEIGHT_COMPARE_OBJECT = 1;
    private static final int WEIGHT_COMPARE_LONG = 1;
    private static final int WEIGHT_VARIABLE_LOAD = 1;
    private static final int WEIGHT_VARIABLE_STORE = 1;
    private static final int WEIGHT_ARITH_ADD_SUB_NEG = 1;
    private static final int WEIGHT_ARITH_MUL = 2;
    private static final int WEIGHT_ARITH_DIV_REM = 5;
    private static final int WEIGHT_ARRAY_LOAD = 2;
    private static final int WEIGHT_ARRAY_STORE = 2;
    private static final int WEIGHT_ARRAY_NEW = 8;

    /**
     * Hilfsmethode: summiert alle Werte einer verketteten Counter-Liste.
     */
    private static long sumCounterList(Object head) {
        long sum = 0;
        try {
            // Reflection, da alle Counter gleich aufgebaut sind
            Object current = head;
            while (current != null) {
                long count = (long) current.getClass().getMethod("getCount").invoke(current);
                sum += count;
                current = current.getClass().getMethod("getPrevious").invoke(current);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sum;
    }

    /**
     * Berechnet die Gesamtkostenmetrik.
     */
    // todo hier fehlen diverse counter
    public static long calculateTotalCost() {
        long total = 0;

        total += sumCounterList(ARITHMETIC_ADD_SUB_NEG.peek()) * WEIGHT_ARITH_ADD_SUB_NEG;
        total += sumCounterList(ARITHMETIC_DIV_REM.peek()) * WEIGHT_ARITH_DIV_REM;
        total += sumCounterList(ARITHMETIC_MUL.peek()) * WEIGHT_ARITH_MUL;

        total += sumCounterList(ARRAY_LOAD.peek()) * WEIGHT_ARRAY_LOAD;
        total += sumCounterList(ARRAY_NEW.peek()) * WEIGHT_ARRAY_NEW;
        total += sumCounterList(ARRAY_STORE.peek()) * WEIGHT_ARRAY_STORE;

        total += sumCounterList(COMPARE_INT.peek()) * WEIGHT_COMPARE_INT;
        total += sumCounterList(COMPARE_LONG.peek()) * WEIGHT_COMPARE_LONG;
        total += sumCounterList(COMPARE_OBJECT.peek()) * WEIGHT_COMPARE_OBJECT;

        total += sumCounterList(JUMP.peek()) * WEIGHT_JUMP;

        total += sumCounterList(METHOD_CALL.peek()) * WEIGHT_METHOD_CALL;
        total += sumCounterList(METHOD_ENTRY.peek()) * WEIGHT_METHOD_ENTRY;

        total += sumCounterList(VARIABLE_LOAD.peek()) * WEIGHT_VARIABLE_LOAD;
        total += sumCounterList(VARIABLE_STORE.peek()) * WEIGHT_VARIABLE_STORE;

        return total;
    }


}
package org.conetex.runtime.instrument;

import org.conetex.runtime.instrument.counter.Config;
import org.conetex.runtime.instrument.interfaces.*;

public class Report {

    public static long add(long a, long b, MathWithMinLongMaxLongConfiguration c) {
        long re = a + b;
        if(b > 0 && re < a){
            System.err.println("overflow: " + a + " > (" + a + " + " + b + " = " + re + ")");
            return c.max();
        }
        if(b < 0 && re > a){
            System.err.println("underflow: " + a + " < (" + a + " + " + b + " = " + re + ")");
            return c.min();
        }
        return re;
    }

    public static long multiply(long a, long b, MathWithMinLongMaxLongConfiguration c) {
        // Schnelle Fälle
        if (a == 0 || b == 0) {
            return 0;
        }

        long result = a * b;

        boolean expectedPositive = (a > 0 && b > 0) || (a < 0 && b < 0);

        if (expectedPositive && result < 0) {
            System.err.println("overflow: " + result + " != " + a + " * " + b);
            return c.max();
        }
        if (!expectedPositive && result > 0) {
            System.err.println("underflow: " + result + " != " + a + " * " + b);
            return c.min();
        }

        return result;
    }

    public static int sum(int[] weights) {
        int weightsSum = 0;
        for (int weight : weights) {
            weightsSum += weight;
        }
        return weightsSum;
    }

    public static ResultDivisionLongByInt calculateWeightedAverage(MathWithMinLongMaxLongConfiguration c, long[] counters, int[] weights) {
        int weightsSumAlsoUsedAsScale = sum(weights);
        return calculateWeightedAverage(weightsSumAlsoUsedAsScale, c, counters, weights, weightsSumAlsoUsedAsScale);
    }

    public static ResultDivisionLongByInt calculateWeightedAverage(int scale, MathWithMinLongMaxLongConfiguration c, long[] counters, int[] weights) {
        return calculateWeightedAverage(scale, c, counters, weights, sum(weights));
    }

    public static ResultDivisionLongByInt calculateWeightedAverage(MathWithMinLongMaxLongConfiguration c, long[] counters, int[] weights, int weightsSum) {
        return calculateWeightedAverage(sum(weights), c, counters, weights, weightsSum);
    }

    public static ResultDivisionLongByInt calculateWeightedAverage(int scale, MathWithMinLongMaxLongConfiguration c, long[] counters, int[] weights, int weightsSum) {
        long weightedAvr = 0;
        long remainder = 0;
        for (int i = 0; i < counters.length; i++) {
            // multiplication may overflow, so we apply division early and we use method multiply that catches this.
            // addition may also overflow if weightSum is not correct, so we use method add that catches this.
            weightedAvr = add(weightedAvr, multiply((counters[i] / weightsSum), weights[i], c), c);  // weightedAvr += (count / weightsSum) * weights[i];
            remainder = add(remainder, multiply(counters[i] % weightsSum, weights[i], c), c);     // remainder += (count % weightsSum) * weights[i];
        }
        long correction = remainder / weightsSum;
        weightedAvr = add(weightedAvr, correction, c);

        int remainingRemainder = (int) (remainder % weightsSum);
        int fraction = (remainingRemainder * scale) / weightsSum;

        return new ResultDivisionLongByInt(
                weightedAvr,
                remainingRemainder,
                (fraction < 0 ? fraction * -1 : fraction)
        );
    }

    // todo above is general math -

    public static void main(String[] args){

        MathWithMinLongMaxLongConfiguration c = new Config(-100, 100);

        // todo dies werden tests:
        int[] weightsPN       = {  5    ,  5  };

        long[] positivDigits1  = {  8L   ,  5L };
        ResultDivisionLongByInt resultP1  = calculateWeightedAverage(c, positivDigits1 , weightsPN);
        System.out.println(resultP1.value() + "." + resultP1.fraction() + " <-----    positivDigits == -1*");

        long[] negativDigits1  = { -8L   , -5L };
        ResultDivisionLongByInt resultN1  = calculateWeightedAverage(c, negativDigits1 , weightsPN);
        System.out.println(resultN1.value() + "." + resultN1.fraction() + " <-----    negativDigits");

        long[] positivDigits0  = new long[]{  8L   ,  11L };
        ResultDivisionLongByInt resultP0  = calculateWeightedAverage(c, positivDigits0 , weightsPN);
        System.out.println(resultP0.value() + "." + resultP0.fraction() + " <-----    positivDigits  == -1*");

        long[] negativDigits0  = new long[]{ -8L   , -11L };
        ResultDivisionLongByInt resultN0  = calculateWeightedAverage(c, negativDigits0 , weightsPN);
        System.out.println(resultN0.value() + "." + resultN0.fraction() + " <-----    negativDigits");


        long[] positivDigits2  = {  8L   ,  8L-13L };
        ResultDivisionLongByInt resultP2  = calculateWeightedAverage(c, positivDigits2 , weightsPN);
        System.out.println(resultP2.value() + "." + resultP2.fraction() + " <-----    positivDigits == 8-6.5 == " + ((8-13)+(13*0.5)));

        long[] negativDigits2  = new long[]{ -8L   , -8L+13L };
        ResultDivisionLongByInt resultN2  = calculateWeightedAverage(c, negativDigits2 , weightsPN);
        System.out.println(resultN2.value() + "." + resultN2.fraction() + " <-----    negativDigits == -8+6.5 == " + ((-8+13)-(13*0.5)));



        System.out.println(" ================ ");
        System.out.println(" ================ ");

        //                    A       B               C               D
        long[] digits10   = { 0L    , 8L            , 6L            , 2L     };
        long[] digits1    = { 7L    , 6L            , 2L            , 5L     };
        int[] weights     = {  1    ,  1            ,  1            , 1      };
        ResultDivisionLongByInt result10 = calculateWeightedAverage(c, digits10, weights);
        ResultDivisionLongByInt result1  = calculateWeightedAverage(c, digits1 , weights);
        System.out.println(result10.value() + "." + result10.fraction() + " " + result1.value() + "." + result1.fraction() + " <-----    result");

        System.out.println(" ================ ");

        //                    A       B               C1      C2      D            we doubled C
        long[] w0Digits10 = { 0L    , 8L            , 6L    , 6L    , 2L     };
        long[] w0Digits1  = { 7L    , 6L            , 2L    , 2L    , 5L     };
        int[] w0Weights   = {  2    ,  2            ,  1    ,  1    ,  2     }; // so C has half weight
        ResultDivisionLongByInt w0Result10 = calculateWeightedAverage(c, w0Digits10, w0Weights);
        ResultDivisionLongByInt w0Result1  = calculateWeightedAverage(c, w0Digits1 , w0Weights);
        System.out.println(w0Result10.value() + "." + w0Result10.fraction() + " " + w0Result1.value() + "." + w0Result1.fraction() + " <----- w0 result");

        System.out.println(" ================ ");

        //                    A       B               C1      C2      D            this works even if we have C1=C+x and C2=C-x.
        long[] w1Digits10 = { 0L    , 8L            , 9L    , 3L    , 2L     };
        long[] w1Digits1  = { 7L    , 6L            , 4L    , 0L    , 5L     };
        int[] w1Weights   = {  2    ,  2            ,  1    ,  1    ,  2     };
        ResultDivisionLongByInt w1Result10 = calculateWeightedAverage(c, w1Digits10, w1Weights);
        ResultDivisionLongByInt w1Result1  = calculateWeightedAverage(c, w1Digits1 , w1Weights);
        System.out.println(w1Result10.value() + "." + w1Result10.fraction() + " " + w1Result1.value() + "." + w1Result1.fraction() + " <----- w1 result");

        System.out.println(" ================ ");

        //                    A       B1      B2      C               D           we doubled B
        long[] w2Digits10 = { 0L    , 9L    , 7L    , 6L            , 2L     };
        long[] w2Digits1  = { 7L    , 4L    , 8L    , 2L            , 5L     };
        int[] w2Weights   = {  2    ,  1    ,  1    ,  2            ,  2     }; // so B has half weight
        ResultDivisionLongByInt w2Result10 = calculateWeightedAverage(c, w2Digits10, w2Weights);
        ResultDivisionLongByInt w2Result1  = calculateWeightedAverage(c, w2Digits1 , w2Weights);
        System.out.println(w2Result10.value() + "." + w2Result10.fraction() + " " + w2Result1.value() + "." + w2Result1.fraction() + " <----- w2 result");


        System.out.println(" ================ ");

        /**/
        // it is not possible to just double the count D because 75% to 25% is not the same as 60% to 40%
        //                       A       B               C               D            we found out D has 2x cost relativ to A
        long[] w3NewDigits10 = { 0L    , 8L            , 6L            , 2L, 2L     }; // so we just count it 2 times
        long[] w3NewDigits1  = { 7L    , 6L            , 2L            , 5L, 5L     }; // so we just count it 2 times
        int[] w3NewWeights   = { 2     , 2             , 2             , 2 , 2      };
        ResultDivisionLongByInt w3NewResult10 = calculateWeightedAverage(c, w3NewDigits10, w3NewWeights);
        ResultDivisionLongByInt w3NewResult1  = calculateWeightedAverage(c, w3NewDigits1 , w3NewWeights);
        System.out.println(w3NewResult10.value() + "." + w3NewResult10.fraction() + " " + w3NewResult1.value() + "." + w3NewResult1.fraction() + " <----- as expected w3New (count 2 times) result is bigger than w2");
        System.out.println(w3NewResult10.remainder() + " " + w3NewResult1.remainder() + " <- w3New rests");

        //                    A       B1      B2      C1      C2      D
        long[] w3Digits10 = { 0L    , 8L            , 6L            , 2*2L     }; // so we just multiply its count by 2
        long[] w3Digits1  = { 7L    , 6L            , 2L            , 2*5L     }; // so we just multiply its count by 2
        int[] w3Weights   = { 2     , 2             , 2             , 2        }; // but we keep the weightsSum 10 from "count it 2 times" above
        ResultDivisionLongByInt w3Result10 = calculateWeightedAverage(c, w3Digits10, w3Weights, 10);
        ResultDivisionLongByInt w3Result1  = calculateWeightedAverage(c, w3Digits1 , w3Weights, 10);
        System.out.println(w3Result10.value() + "." + w3Result10.fraction() + " " + w3Result1.value() + "." + w3Result1.fraction() + " <----- as expected w3 (2*counter) result is bigger than w2");
        System.out.println(w3Result10.remainder() + " " + w3Result1.remainder() + " <- w3 rests");

        System.out.println(" ================ ");

        //                    A       B1      B2      C1      C2      D
        long[] w4Digits10 = { 0L    , 8L            , 6L            , 2L     };
        long[] w4Digits1  = { 7L    , 6L            , 2L            , 5L     };
        int[] w4Weights   = { 2     , 2             , 2             , 4      }; // it is the same like weight it 2x
        ResultDivisionLongByInt w4Result10 = calculateWeightedAverage(c, w4Digits10, w4Weights);
        ResultDivisionLongByInt w4Result1  = calculateWeightedAverage(c, w4Digits1 , w4Weights);
        System.out.println(w4Result10.value() + "." + w4Result10.fraction() + " " + w4Result1.value() + "." + w4Result1.fraction() + " <----- w4 result");
        System.out.println(w4Result10.remainder() + " " + w4Result1.remainder() + " <- w4 rests");

        //                     A       B1      B2      C1      C2      D
        long[] w4Digits10b = { 0L    , 8L            , 6L            , 2L     };
        long[] w4Digits1b  = { 7L    , 6L            , 2L            , 5L     };
        int[] w4Weightsb   = { 1     , 1             , 1             , 2      };                  // weights must be in right relation to each other.
        ResultDivisionLongByInt w4Result10b = calculateWeightedAverage(10, c, w4Digits10b, w4Weightsb); // but to get an understandable fraction, weightsSum has not be 10.
        ResultDivisionLongByInt w4Result1b  = calculateWeightedAverage(10, c, w4Digits1b , w4Weightsb); // we can achieve this by setting scale to 10.
        System.out.println(w4Result10b.value() + "." + w4Result10b.fraction() + " " + w4Result1b.value() + "." + w4Result1b.fraction() + " <----- w4b result");
        System.out.println(w4Result10b.remainder() + " " + w4Result1b.remainder() + " <- w4b rests");

        System.out.println(" ================ ");

    }

    public static long[] calculateTotalCost(RetransformingClassFileTransformer transformer) {
        long[] result = new long[1];
        int[] weights = transformer.getCounterWeights();

        CounterStub[] stacks = transformer.getCounters();
        Counter[] counters = new Counter[stacks.length];
        for (int i = 0; i < stacks.length; i++) {
            counters[i] = stacks[i].peek();
        }

        Configuration configuration = transformer.getConfig();

        result[0] = calculateWeightedAverage(configuration, counters, weights).value();
        counters = configuration.countPreviousOnAll(counters);

        int i = 0;
        while (configuration.containsCountableCounters(counters)) {
            // increase result
            long[] newResult = new long[result.length + 1];
            System.arraycopy(result, 0, newResult, 1, result.length);
            result = newResult;

            // store result part
            result[0] = calculateWeightedAverage(configuration, counters, weights).value();

            /* todo debug fix
[0, -3, -5, 0, -3]

-16
[0, -2, -5, -3, -2]
0
[0, 2, 7, 13, 14] warum 0?    2 + 7 ist unplausible
            */

            // prepare next level
            counters = configuration.countPreviousOnAll(counters);
            i++;
        }
        return result;
    }

    private static long[] transformToLong(Counter[] counters) {
        long[] countersRaw = new long[counters.length];
        for (int i = 0; i < counters.length; i++) {
            countersRaw[i] = counters[i].getValue();
        }
        return countersRaw;
    }

    private static ResultDivisionLongByInt calculateWeightedAverage(MathWithMinLongMaxLongConfiguration c, Counter[] counters, int[] weights) {
        return calculateWeightedAverage(c, transformToLong(counters), weights);
    }

    private static ResultDivisionLongByInt calculateWeightedAverage(int scale, MathWithMinLongMaxLongConfiguration c, Counter[] counters, int[] weights) {
        return calculateWeightedAverage(scale, c, transformToLong(counters), weights);
    }

    private static ResultDivisionLongByInt calculateWeightedAverage(MathWithMinLongMaxLongConfiguration c, Counter[] counters, int[] weights, int weightsSum) {
        return calculateWeightedAverage(c, transformToLong(counters), weights, weightsSum);
    }

    private static ResultDivisionLongByInt calculateWeightedAverage(int scale, MathWithMinLongMaxLongConfiguration c, Counter[] counters, int[] weights, int weightsSum ) {
        return calculateWeightedAverage(scale, c, transformToLong(counters), weights, weightsSum);
    }

}

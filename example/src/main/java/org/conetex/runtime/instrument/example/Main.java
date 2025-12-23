package org.conetex.runtime.instrument.example;



import org.conetex.runtime.instrument.example.subpackage.ClassFromOtherPackage;
import org.conetex.runtime.instrument.metrics.cost.CostCounters;

import java.io.File;
import java.util.TreeMap;

public class Main {

    // -javaagent:/agent/target/agent-0.0.0-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.0-SNAPSHOT.jar
    public static void main(String[] args) {
        System.out.println("working here: " + new File(".").getAbsolutePath());
        System.out.println("S T A R T");
        System.out.println("Example Counter at start: " + CostCounters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps at start: " + CostCounters.JUMP.peek().getValue() + " ");
        //MethodCalls.head.count = Long.MIN_VALUE;
        System.out.println("Example Counter at reset: " + CostCounters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps at reset: " + CostCounters.JUMP.peek().getValue() + " ");

        System.out.println("Example Counter before real nothing: " + CostCounters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps before real nothing: " + CostCounters.JUMP.peek().getValue() + " ");
        System.out.println("Example Jumps after real nothing: " + CostCounters.JUMP.peek().getValue() + " ");
        System.out.println("Example Counter after real nothing: " + CostCounters.METHOD_ENTRY.peek().getValue() + " ");

        System.out.println("Example Counter before nothing: " + CostCounters.METHOD_ENTRY.peek().getValue() + " ");
        System.out.println("Example Jumps before nothing: " + CostCounters.JUMP.peek().getValue() + " ");
        nothing();
        System.out.println("Example Jumps after nothing: " + CostCounters.JUMP.peek().getValue() + " ");
        System.out.println("Example Counter after nothing: " + CostCounters.METHOD_ENTRY.peek().getValue() + " ");

        long i = CostCounters.METHOD_ENTRY.peek().getValue();
        System.out.println("loaded counter..." + i);
        long xx = Long.MAX_VALUE;
        String xxx = Long.toString(xx);
        System.out.println("Example Jumps at toString: " + CostCounters.JUMP.peek().getValue() + " ");
        System.out.println("Example Counter: " + CostCounters.METHOD_ENTRY.peek().getValue() + " " + xxx);
        i = CostCounters.METHOD_ENTRY.peek().getValue();
        System.out.println("loaded counter..." + i);
        System.out.println("load counter..." + xx);
        System.out.println("Example Counter: ".concat(Long.toString(CostCounters.METHOD_ENTRY.peek().getValue())) );
        ClassFromOtherPackage.test();
        i = CostCounters.METHOD_ENTRY.peek().getValue();
        System.out.println("loaded counter..." + i);
        foo();
        System.out.println("Example Counter: " + CostCounters.METHOD_ENTRY.peek().getValue());
        Methods.foo();
        System.out.println("Example Counter: " + CostCounters.METHOD_ENTRY.peek().getValue());
        Methods.bar();
        System.out.println("Example Counter: " + CostCounters.METHOD_ENTRY.peek().getValue());
        bar();
        System.out.println("Example Counter: " + CostCounters.METHOD_ENTRY.peek().getValue());

        TreeMap<String, String> treeX = new TreeMap<>();
        System.out.println("Example Counter x: ".concat( Long.valueOf(CostCounters.METHOD_ENTRY.peek().getValue()).toString() ));
        treeX.put("x", "xx");

        String isInMap = treeX.get("x");
        System.out.println("loaded counter..." + isInMap);
        System.out.println("Example Counter x: ".concat( Long.valueOf(CostCounters.METHOD_ENTRY.peek().getValue()).toString() ));
        System.out.println("Example Counter x: ".concat( Long.valueOf(CostCounters.METHOD_ENTRY.peek().getValue()).toString() ));

    }

    static void nothing() {
    }

    static void foo() {
        System.out.println("foo");
    }

    static void bar() {
        System.out.println("bar");
    }

}
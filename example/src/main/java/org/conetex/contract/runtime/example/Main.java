package org.conetex.contract.runtime.example;



import org.conetex.contract.runtime.example.subpackage.ClassFromOtherPackage;
import org.conetex.contract.runtime.instrument.Counter;

import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {

        System.out.println("S T A R T");
        System.out.println("Example Counter at start: " + Counter.count + " ");
        Counter.count = Long.MIN_VALUE;
        System.out.println("Example Counter at reset: " + Counter.count + " ");

        long i = Counter.count;
        Long x = Long.valueOf(Long.MAX_VALUE);
        long i1 = Counter.count;
        Long xx = x.longValue();
        String xxx = xx.toString();
        long i2 = Counter.count;
        System.out.println("Example Counter: " + Counter.count + " " + xxx);
        i = Counter.count;
        System.out.println("load counter..." + x);
        System.out.println("Example Counter: ".concat(Long.toString(Counter.count)) );
        ClassFromOtherPackage.test();
        i = Counter.count;
        foo();
        System.out.println("Example Counter: " + Counter.count);
        Methods.foo();
        System.out.println("Example Counter: " + Counter.count);
        Methods.bar();
        System.out.println("Example Counter: " + Counter.count);
        bar();
        System.out.println("Example Counter: " + Counter.count);

        TreeMap<String, String> treeX = new TreeMap<>();
        System.out.println("Example Counter x: ".concat( Long.valueOf(Counter.count).toString() ));
        treeX.put("x", "xx");
        StackTraceElement yyy = null;
        //IdentityHashMap.IdentityHashMapIterator xxxxx = null;
        String isInMap = treeX.get("x");
        System.out.println("Example Counter x: ".concat( Long.valueOf(Counter.count).toString() ));
        System.out.println("Example Counter x: ".concat( Long.valueOf(Counter.count).toString() ));

    }

    static void foo() {
        System.out.println("foo");
    }

    static void bar() {
        System.out.println("bar");
    }

}
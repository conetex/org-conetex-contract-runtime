package org.conetex.contract.runtime.example;

import org.conetex.contract.runtime.instrument.Counter;
import java.util.TreeMap;

public class Methods {

	public static void foo() {
        System.out.println("foo 2");
    }

	public static void bar() {
    	System.out.println("Example Counter b: ".concat( Long.valueOf(Counter.count).toString() ));
        System.out.println("bar 2");
        TreeMap<String, String> x = new TreeMap<>();
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counter.count).toString() ));
    	//x.put("x", "xx");
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counter.count).toString() ));
    	x.put("x", "xx");
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counter.count).toString() ));
    	x.put("x", "xx");
        String test = x.get("x");
        System.out.println( test );
    	System.out.println("Example Counter c: ".concat( Long.valueOf(Counter.count).toString() ));

    	System.out.println("Example Counter d: ".concat( Long.valueOf(Counter.count).toString() ));

    }
	
}

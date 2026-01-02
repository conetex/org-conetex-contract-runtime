package org.conetex.runtime.instrument.agent;

import java.lang.invoke.*;

public class Bootstrap {
    public static CallSite bootstrap(MethodHandles.Lookup lookup, String name, MethodType type,
                                     Class<?> realOwner) throws NoSuchMethodException, IllegalAccessException {
        // Link the dynamic method to an actual implementation elsewhere (possibly in another class)
        MethodHandle targetMethodHandle = lookup.findStatic(
                realOwner, // Real owner of the "incrementCompareInt" method
                name,      // Actual method name
                type       // Method descriptor
        );

        // Return a CallSite that links to the targetMethodHandle
        return new ConstantCallSite(targetMethodHandle);
    }
}

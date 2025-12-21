package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.Report;
import org.conetex.runtime.instrument.counter.Stack;
import org.conetex.runtime.instrument.interfaces.Counter;
import org.conetex.runtime.instrument.interfaces.RetransformingClassFileTransformer;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class Transformer implements RetransformingClassFileTransformer {

    private String mainClassJvmName;

    @Override
    public void initMainClassJvmName(String mainClassJvmName) {
        this.mainClassJvmName = mainClassJvmName;
    }

    @Override
    public Counter[] getCounters() {
        return new Counter[]{
                Counters.ARITHMETIC_ADD_SUB_NEG.peek(),
                Counters.ARITHMETIC_DIV_REM.peek(),
                Counters.ARITHMETIC_MUL.peek(),
                Counters.ARRAY_LOAD.peek(),
                Counters.ARRAY_NEW.peek(),
                Counters.ARRAY_STORE.peek(),
                Counters.COMPARE_INT.peek(),
                Counters.COMPARE_LONG.peek(),
                Counters.COMPARE_OBJECT.peek(),
                Counters.EXCEPTION_THROW.peek(),
                Counters.FIELD_LOAD.peek(),
                Counters.FIELD_STORE.peek(),
                Counters.JUMP.peek(),
                Counters.METHOD_CALL.peek(),
                Counters.METHOD_ENTRY.peek(),
                Counters.MONITOR.peek(),
                Counters.VARIABLE_LOAD.peek(),
                Counters.VARIABLE_STORE.peek(),
                Counters.TYPE_CHECK.peek()
        };
    }

    public double[] xgetCounterWeightsDouble() {
        return WEIGHTS_DOUBLE;
    }

    @Override
    public int[] getCounterWeights() {
        return WEIGHTS;
    }

    @Override
    public void resetCounters() {
        Counters.ARITHMETIC_ADD_SUB_NEG.reset();
        Counters.ARITHMETIC_DIV_REM.reset();
        Counters.ARITHMETIC_MUL.reset();
        Counters.ARRAY_LOAD.reset();
        Counters.ARRAY_NEW.reset();
        Counters.ARRAY_STORE.reset();
        Counters.COMPARE_INT.reset();
        Counters.COMPARE_LONG.reset();
        Counters.COMPARE_OBJECT.reset();
        Counters.EXCEPTION_THROW.reset();
        Counters.FIELD_LOAD.reset();
        Counters.FIELD_STORE.reset();
        Counters.JUMP.reset();
        Counters.METHOD_CALL.reset();
        Counters.METHOD_ENTRY.reset();
        Counters.MONITOR.reset();
        Counters.VARIABLE_LOAD.reset();
        Counters.VARIABLE_STORE.reset();
        Counters.TYPE_CHECK.reset();
    }

    private final Set<String> handledClasses;

    @Override
    public Set<String> getHandledClasses() {
        return handledClasses;
    }

    private final Set<String> transformFailedClasses;

    @Override
    public Set<String> getTransformFailedClasses() {
        return transformFailedClasses;
    }

    private final Set<String> transformSkippedClasses;

    @Override
    public Set<String> getTransformSkippedClasses() {
        return transformSkippedClasses;
    }

    public final static int xWEIGHT_BASE = 1000000; // count of 0s are equal to the count of digits in the original weight see costWeights.md

    public final static int[] WEIGHTS = new int[] {
            35,//165, // ArithmeticAddSubNeg
           109,//890, // ArithmeticDivRem
            43,//956, // ArithmeticMul
            48,//352, // ArrayLoad
            98,//901, // ArrayNew
            65,//934, // ArrayStore
            30,//769, // CompareInt
            39,//560, // CompareLong
            43,//956, // CompareObject
           109,//890, // ExceptionThrow
            52,//747, // FieldLoad
            61,//539, // FieldStore
            28,//571, // Jump
            54,//945, // MethodCall
             0,//  0, // MethodEntry
            76,//923, // Monitor
            50,//550, // VariableLoad
            21,//978, // VariableStore
            26,//374  // TypeCheck
    };

    public final static double[] WEIGHTS_DOUBLE = new double[] {
            0.035165, // ArithmeticAddSubNeg
            0.109890, // ArithmeticDivRem
            0.043956, // ArithmeticMul

            0.048352, // ArrayLoad
            0.098901, // ArrayNew
            0.065934, // ArrayStore

            0.030769, // CompareInt
            0.039560, // CompareLong
            0.043956, // CompareObject

            0.109890, // ExceptionThrow

            0.052747, // FieldLoad
            0.061539, // FieldStore

            0.028571, // Jump

            0.054945, // MethodCall
            0.0     , // MethodEntry

            0.076923, // Monitor

            0.050550, // VariableLoad
            0.021978, // VariableStore

            0.026374  // TypeCheck
    };


    public int xgetCounterWeightsBase() {
        return xWEIGHT_BASE;
    }

    public Transformer() {
        this.handledClasses = new TreeSet<>();
        this.transformFailedClasses = new TreeSet<>();
        this.transformSkippedClasses = new TreeSet<>();

        // creating this array leads to
        // load all classes, before they are needed by transform.
        // this is necessary to avoid transformation loops
        Counters.echo("egal");
        this.resetCounters();
        this.getCounters();
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        System.out.println("transform: " + loader + " (loader) | " + classJvmName +
                " (classJvmName) | " + classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain) | " + module + "(module)");
        return transform(loader, classJvmName, classBeingRedefined, protectionDomain, classfileBuffer);
    }

    @Override
    public byte[] transform(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        if (classJvmName.equals(mainClassJvmName)) {
            System.out.println("transform mainClass: " + classJvmName + " ");
        }

        if (this.handledClasses.contains(classJvmName)) {
            System.out.println("transform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            return classfileBuffer;
        }

        this.handledClasses.add(classJvmName);

        if (classJvmName.contains("org/objectweb/asm/")
                || classJvmName.startsWith("org/conetex/runtime/instrument")
        ) { // skip transform
            System.out.println("t noTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            this.transformSkippedClasses.add(classJvmName);
            return classfileBuffer;
        }

        System.out.println("t doTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");

        try {
            return transform(classfileBuffer);
        }
        catch (Throwable e) {
            System.err.println("t !!! exception 4 " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        return classfileBuffer;
    }

    @Override
    public void triggerRetransform(Instrumentation inst, Class<?>[] allClasses) {
        for (Class<?> clazz : allClasses) {
            String classJvmName = clazz.getName().replace('.', '/');

            System.out.println("retransform .....: '" + classJvmName + "' (classJvmName)");


            if(   this.handledClasses.contains( classJvmName )   ) {
                System.out.println("retransform obsolete: '" + classJvmName +
                        "' (classJvmName) is already transformed");
                continue;
            }

            if (! inst.isModifiableClass(clazz)) {
                System.out.println("retransform skipped for unmodifiable: '" + classJvmName + "' (classJvmName)");
                continue;
            }

/*
            // TODO maybe obsolete
            if( classJvmName.contains("org/objectweb/asm/") ||
                    classJvmName.startsWith("org/conetex/runtime/instrument")
                    || classJvmName.startsWith("org/conetex/runtime/Agent")
//                    || classJvmName.startsWith("java/util/TreeSet")
            ) { // skip retransform
                System.out.println("retransform skipped: '" + classJvmName + "' (classJvmName)");
                continue;
            }
*/
            try {
                inst.retransformClasses(clazz);
            } catch (UnmodifiableClassException e) {
                System.err.println("retransform failed for'" + clazz + "' (class). UnmodifiableClassException: " +
                        e.getMessage());
                continue;
            }
            System.out.println("retransform triggered for '" + clazz + "' (class) || '" + classJvmName +
                    "' (classJvmName) -->");
        }
    }

    private static byte[] transform(byte[] classBytes) {
        System.out.println(" classWriter->");
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        ClassVisitor visitor = new Visitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        byte[] re = writer.toByteArray();
        System.out.println(" <-classWriter");
        return re;
    }

    public static byte[] noRealTransform(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        return writer.toByteArray();
    }

    public static byte[] noTransform(byte[] classBytes) {
        return classBytes;
    }

    @Override
    public long[] report(){
        System.out.println("Long min:  " + Long.MIN_VALUE);
        System.out.println("Long max:  " + Long.MAX_VALUE);
        System.out.println("AbstractCounter min:  " + Stack.COUNTER_MIN_VALUE);
        System.out.println("AbstractCounter max:  " + Stack.COUNTER_MAX_VALUE);
        System.out.println("Shutdown-Hook started.");
        long[] totalCost =  Report.calculateTotalCost(this);
        System.out.println("overall costs:");
        System.out.println(Arrays.toString(totalCost));
        return totalCost;
    }


}

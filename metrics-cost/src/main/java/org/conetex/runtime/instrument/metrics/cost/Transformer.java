package org.conetex.runtime.instrument.metrics.cost;

import org.conetex.runtime.instrument.counter.CountersWeighted;
import org.conetex.runtime.instrument.interfaces.RetransformingClassFileTransformer;
import org.conetex.runtime.instrument.interfaces.arithmetic.ResultLongDividedByInt;
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

    public static final String UNTRANSFORMABLE_PACKAGE_SELF = "org/conetex/runtime/instrument";

    public static final String UNTRANSFORMABLE_CLASSES_SELF_TEST = "org/conetex/runtime/instrument/test/jar/Main";
    public static final String UNTRANSFORMABLE_CLASSES_SELF_TEST_M = "org/conetex/runtime/instrument/bootstrap/Bootstrap";

    public static final String UNTRANSFORMABLE_PACKAGE_LIBRARY_ASM = "org/objectweb/asm/";

    public static final int STATUS_BLOCKED = 403;

    private String mainClassJvmName;

    @Override
    public void initMainClassJvmName(String mainClassJvmName) {
        this.mainClassJvmName = mainClassJvmName;
    }

    public void addToHandledClasses(String classJvmName) {
        this.handledClasses.add(classJvmName);
    }

    @Override
    public CountersWeighted getConfig() {
        return Counters.CONFIG;
    }

    @Override
    public void resetCounters() {
        Counters.reset();
    }

    @Override
    public void blockIncrement(boolean incrementationBlocked) {
        Counters.blockIncrement(incrementationBlocked);
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


    public Transformer() {
        this.handledClasses = new TreeSet<>();
        this.transformFailedClasses = new TreeSet<>();
        this.transformSkippedClasses = new TreeSet<>();

        // calling this leads to
        // load all classes, before they are needed by transform.
        // this is necessary to avoid transformation loops

        //this.getConfig();
        //System.out.println("Counters: " + Counters.class.getModule() + "(module) " + Counters.class.getClassLoader() + "(classLoader)");
        //Counters.reset();
    }

    @Override
    public byte[] transform(Module module, ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        System.out.println("transform: " + loader + " (loader) | " + classJvmName +
                " (classJvmName) | " + classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain) | " + module + "(module)");
        return transform(loader, classJvmName, classBeingRedefined, protectionDomain, classFileBuffer);
        //return classFileBuffer;
    }

    @Override
    public byte[] transform(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {

        if (classJvmName.equals(mainClassJvmName)) {
            System.out.println("transform mainClass: " + classJvmName + " ");
        }

        if (this.handledClasses.contains(classJvmName)) {
            System.out.println("transform already done: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            return classFileBuffer;
        }

        this.handledClasses.add(classJvmName);

        if ( classJvmName.startsWith(UNTRANSFORMABLE_PACKAGE_LIBRARY_ASM) ||
                classJvmName.startsWith(UNTRANSFORMABLE_CLASSES_SELF_TEST)  ||
                classJvmName.startsWith(UNTRANSFORMABLE_CLASSES_SELF_TEST_M) ||

                classJvmName.startsWith("java") ||
                classJvmName.startsWith("jdk") ||
                classJvmName.startsWith("sum") ||

                classJvmName.startsWith("java/lang/invoke/") ||
                classJvmName.startsWith("java/util/Objects") ||
                classJvmName.startsWith("java/util/Arrays") ||

                classJvmName.startsWith("sun/instrument") ||
                classJvmName.startsWith("java/lang/reflect") ||
                classJvmName.startsWith("jdk/internal")  ||

                classJvmName.startsWith("java/util/TreeSet") ||
                classJvmName.startsWith("java/lang/String") ||
                classJvmName.startsWith("java/io") ||
                classJvmName.startsWith("java/nio") ||
                classJvmName.startsWith("sun/nio") ||
                classJvmName.startsWith("java/util/concurrent") ||
                classJvmName.startsWith("com/intellij")



                ) {
            System.out.println("t noTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            // skip transform
            this.transformSkippedClasses.add(classJvmName);
            return classFileBuffer;
        }

        if ( classJvmName.startsWith(UNTRANSFORMABLE_PACKAGE_SELF) ) {
            System.err.println("blocked " + classJvmName);
            // BLOCK class
            // since this class should have been loaded before transformer was added to instrumentation.
            // retransform for this class should have been skipped.
            //throw new RuntimeException("blocked " + classJvmName);
            System.err.println("blocked transform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                    classBeingRedefined + " (classBeingRedefined) | " +
                    (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
            //Runtime.getRuntime().halt(STATUS_BLOCKED);
            System.exit(STATUS_BLOCKED);
        }

        System.out.println("t doTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");

        try {
            return transform(classFileBuffer);
        }
        catch (Throwable e) {
            System.err.println("t !!! exception 4 " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        return classFileBuffer;
    }

    @Override
    public void triggerRetransform(Instrumentation inst, Class<?>[] allClasses) {
        int i = 0;
        for (Class<?> clazz : allClasses) {
            System.out.println("retransform " + (i++) + "/" + allClasses.length);
            String classJvmName = clazz.getName().replace('.', '/');

//            System.out.println("retransform .....: '" + classJvmName + "' (classJvmName) '" + clazz.getModule() + "' (module) '" + clazz.getClassLoader() + "' (classLoader)");


            if(   this.handledClasses.contains( classJvmName )   ) {
                System.out.println("retransform obsolete: '" + classJvmName +
                        "' (classJvmName) is already transformed");
                continue;
            }

            if (! inst.isModifiableClass(clazz)) {
                System.out.println("retransform skipped for unmodifiable: '" + classJvmName + "' (classJvmName)");
                continue;
            }
			
			// maybe obsolete
            if( classJvmName.startsWith(UNTRANSFORMABLE_PACKAGE_LIBRARY_ASM) ||
                    classJvmName.startsWith(UNTRANSFORMABLE_PACKAGE_SELF) ||
                    classJvmName.startsWith("java/lang/") ||
                    classJvmName.startsWith("jdk/internal")
            ) { // skip retransform
                System.out.println("retransform skipped: '" + classJvmName + "' (classJvmName)");
                continue;
            }

            try {
                inst.retransformClasses(clazz);
            } catch (UnmodifiableClassException e) {
                System.err.println("retransform failed for'" + clazz + "' (class). UnmodifiableClassException: " +
                        e.getMessage());
                continue;
            }
            catch (Exception e) {
                System.err.println("retransform failed for'" + clazz + "' (class). Exception: " +
                        e.getMessage());
                continue;
            }
            catch (Error e) {
                System.err.println("retransform failed for'" + clazz + "' (class). Error: " +
                        e.getMessage());
                continue;
            }
            catch (Throwable e) {
                System.err.println("retransform failed for'" + clazz + "' (class). Throwable: " +
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

    @SuppressWarnings("unused")
    public static byte[] noRealTransform(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        return writer.toByteArray();
    }

    @SuppressWarnings("unused")
    public static byte[] noTransform(byte[] classBytes) {
        return classBytes;
    }

    @Override
    public ResultLongDividedByInt[] report(){
        ResultLongDividedByInt[] totalCost =  Report.calculateTotalCost(this);
        System.out.println("overall costs:");
        System.out.println(Arrays.toString(totalCost));
        return totalCost;
    }


}

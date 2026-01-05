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

    public static final String[] UNTRANSFORMABLE_PACKAGES = {
            "java/lang/invoke/" , // needed for bootstrap calls

            // unblock test-classes
            "org/conetex/runtime/instrument/test/jar/Main" ,      // todo protect this better (nobody should create a package like this)
            "org/conetex/runtime/instrument/test/jar/module/Main" // maybe: class-does not end with "/" so switch from "startswith" to "equals"

            // "java/lang/invoke/MethodHandle$1" ,
            // "sun" ,
            // "com/intellij/rt" ,
            // "java/io/UnsupportedEncodingException",

            /* todo only needed for dynamic mode and it isn't enough yet...
            "java/lang/reflect" ,
            "com/intellij/rt" ,
            "jdk.internal.loader.ClassLoaders" ,
            "sun/invoke" ,
            "java/security" ,
            "java/lang/Throwable",
            "java/lang"
            */
    };

    public static final String[] BLOCKED_PACKAGES = {
            "org/conetex/runtime/instrument",
            "org/objectweb/asm/"
    };

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

        // todo is this solved in general?
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
    }

    private static boolean transformInProgress = false;

    @Override
    public byte[] transform(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classFileBuffer) {
        if(transformInProgress){
            throw new CyclicCallException("circle");
        }
        transformInProgress = true;
        try {
            return transformIntern( loader,  classJvmName,  classBeingRedefined,
                     protectionDomain,  classFileBuffer);
        }
        finally{
            transformInProgress = false;
        }
    }

    private byte[] transformIntern(ClassLoader loader, String classJvmName, Class<?> classBeingRedefined,
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

        for(String untransformablePackage : UNTRANSFORMABLE_PACKAGES){
            if ( classJvmName.startsWith(untransformablePackage) ) {
                System.out.println("t noTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                        classBeingRedefined + " (classBeingRedefined) | " +
                        (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                // skip transform
                this.transformSkippedClasses.add(classJvmName);
                return classFileBuffer;
            }
        }

        for(String blockedPackage : BLOCKED_PACKAGES){
            if ( classJvmName.startsWith(blockedPackage) ) {
                System.err.println("blocked transform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                        classBeingRedefined + " (classBeingRedefined) | " +
                        (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");
                block(classJvmName);
            }
        }

        System.out.println("t doTransform: " + loader + " (loader) | " + classJvmName + " (classJvmName) | " +
                classBeingRedefined + " (classBeingRedefined) | " +
                (protectionDomain == null ? "null" : protectionDomain.hashCode()) + " (protectionDomain)");

        try {
            return transform(classFileBuffer);
        }
        catch(CyclicCallException e){
            System.err.println("circle " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        catch (Throwable e) {
            System.err.println("t !!! exception 4 " + classJvmName + " | " + e.getClass().getName() + " | " +
                    e.getMessage());
            this.transformFailedClasses.add(classJvmName);
        }
        return classFileBuffer;
    }

    private void block(String classJvmName) {
        System.err.println("blocked " + classJvmName);
        // BLOCK class
        // since this class should have been loaded before transformer was added to instrumentation.
        // retransform for this class should have been skipped.
        // todo is there any chance to make this nicer?
        //throw new RuntimeException("blocked " + classJvmName);
        //Runtime.getRuntime().halt(STATUS_BLOCKED);
        System.exit(STATUS_BLOCKED);
    }

    @Override
    public void triggerRetransform(Instrumentation inst, Class<?>[] allClasses) {
        classLoop: for (Class<?> clazz : allClasses) {
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
            for(String untransformablePackage : UNTRANSFORMABLE_PACKAGES){
                if( classJvmName.startsWith(untransformablePackage) ) { // skip retransform
                    System.out.println("retransform skipped: '" + classJvmName + "' (classJvmName)");
                    continue classLoop;
                }
            }
            for(String blockedPackage : BLOCKED_PACKAGES){
                if ( classJvmName.startsWith(blockedPackage) ) {
                    block(classJvmName);
                }
            }

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

    public static class CyclicCallException extends IllegalStateException {
        public CyclicCallException(String message) {
            super(message);
        }
    }

    private static synchronized byte[] transform(byte[] classBytes) {
        System.out.println(" classWriter->");
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        //ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new Visitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        //reader.accept(visitor, ClassReader.SKIP_FRAMES);
        byte[] re = writer.toByteArray();
        System.out.println(" <-classWriter");
        return re;
    }

    @SuppressWarnings("unused")
    public static byte[] noRealTransform(byte[] classBytes) {
        // todo why does this lead to errors?
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);
        return writer.toByteArray();
    }

    @SuppressWarnings("unused")
    public static byte[] noTransform(byte[] classBytes) {
        // todo why does this lead to errors?
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

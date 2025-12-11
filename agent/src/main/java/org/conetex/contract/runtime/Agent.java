package org.conetex.contract.runtime;

import org.conetex.contract.runtime.instrument.RetransformingClassFileTransformer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

public class Agent {

    public static final String ARG_PATH_TO_TRANSFORMER_JAR = "pathToTransformerJar";

    public static void agentmain(
            String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    // USAGE: -javaagent:C:\_PROJ\GITHUB\org.conetex.contract.runtime\agent\target\agent-1.0-SNAPSHOT.jar=C:\_PROG\eclipse-java-2025-06WSpaces\workspaceA\counter\target\counter-0.0.2-SNAPSHOT-jar-with-dependencies.jar
    // USAGE: -javaagent:/agent/target/agent-1.0-SNAPSHOT.jar=C:\_PROG\eclipse-java-2025-06WSpaces\workspaceA\counter\target\counter-0.0.2-SNAPSHOT-jar-with-dependencies.jar
    public static void premain(String agentArgs, Instrumentation inst) {
        Path agentPath;
        try {
            agentPath = Paths.get(Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        Path agentDir = Files.isDirectory(agentPath) ? agentPath : agentPath.getParent();

        Map<String, String> args = parseAgentArgs(agentArgs);

        String bootstrapJarPath = args.get(ARG_PATH_TO_TRANSFORMER_JAR);
        if (bootstrapJarPath == null || bootstrapJarPath.isEmpty()) {
            throw new IllegalArgumentException("Missing required agent argument: " + ARG_PATH_TO_TRANSFORMER_JAR + "=<path-to-bootstrap-jar>");
        }
        Path bootstrapPath = agentDir.resolve(bootstrapJarPath);
        JarFile bootstrapJar;
        try {
            bootstrapJar = new JarFile(bootstrapPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String appendToBootstrapClassLoaderSearchStr = getMainAttributeFromJar(bootstrapJar, "appendToBootstrapClassLoaderSearch");
        assert appendToBootstrapClassLoaderSearchStr != null;
        if(! appendToBootstrapClassLoaderSearchStr.equals("false")){
            inst.appendToBootstrapClassLoaderSearch(bootstrapJar);
        }

        System.out.println("Redefine supported: " + inst.isRedefineClassesSupported());
        System.out.println("Retransform supported: " + inst.isRetransformClassesSupported());
        System.out.println("NativeMethodPrefix supported: " + inst.isNativeMethodPrefixSupported());

        String command = System.getProperty("sun.java.command");
        System.out.println("sun.java.command: " + command);
        assert command != null;

        String mainClassJavaStr;
        if(command.endsWith(".jar")) {
            mainClassJavaStr = getMainAttributeFromJar(command, "Main-Class");
            System.out.println("mainClassStr of jar from sun.java.command: " + mainClassJavaStr);
            System.out.println("Build-Jdk-Spec of jar from sun.java.command: " + getMainAttributeFromJar(command, "Build-Jdk-Spec"));
        }
        else {
            mainClassJavaStr = command;
            System.out.println("mainClassStr equals sun.java.command");
            System.out.println("Build-Jdk-Spec unknown, because command does not end with jar");
        }
        assert mainClassJavaStr != null;
        String mainClassJvmStr = mainClassJavaStr.replace('.', '/');
        System.out.println("mainClassJvmStr from sun.java.command: " + mainClassJvmStr);

        Class<?>[] classes = inst.getAllLoadedClasses();
        System.out.println("allLoadedClasses size: " + classes.length);

        String transformerClassStr = getMainAttributeFromJar(bootstrapJar, "Transformer-Class");
        Class<?> transformerClass;
        try {
            transformerClass = Class.forName(transformerClassStr, true, ClassLoader.getSystemClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        System.out.println("==> addTransformer '" + agentArgs + "' | '" + inst + "'");
        RetransformingClassFileTransformer transformer;
        try {
            transformer = (RetransformingClassFileTransformer) transformerClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        transformer.initMainClassJvmName(mainClassJvmStr);
        System.out.println("createdTransformer " + transformer);

        inst.addTransformer(transformer, true);
        System.out.println("<== addTransformer " );

        transformer.triggerRetransform(inst, inst.getAllLoadedClasses());
        System.out.println("...");
    }

    private static String getMainAttributeFromJar(String jarPath, String attributeName) {
        File jarFile = new File(jarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            System.err.println("The specified file does not exist or is not a valid file.");
            return null;
        }
        return getMainAttributeFromJar(jarFile, attributeName);
    }

    private static String getMainAttributeFromJar(File jarFile, String attributeName) {
        try (JarInputStream jarStream = new JarInputStream(new FileInputStream(jarFile))) {
            Manifest manifest = jarStream.getManifest();
            if (manifest != null) {
                Attributes attributes = manifest.getMainAttributes();
                String mainClass = attributes.getValue(attributeName);
                if (mainClass != null) {
                    System.out.println("Main-Class: " + mainClass);
                } else {
                    System.out.println("No Main-Class attribute found in the manifest.");
                }
                return mainClass;
            } else {
                System.out.println("No manifest found in the JAR file.");
            }
        } catch (Exception e) {
            System.err.println("Error reading the JAR file: " + e.getMessage());
        }
        return null;
    }

    public static String getMainAttributeFromJar(JarFile jarFile, String attributeName) {
        Manifest manifest;
        try {
            manifest = jarFile.getManifest();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (manifest == null) return null;
        Attributes attrs = manifest.getMainAttributes();
        if (attrs == null) return null;
        return attrs.getValue(attributeName);
    }

    private static Map<String,String> parseAgentArgs(String agentArgs) {
        Map<String,String> map = new TreeMap<>();
        if (agentArgs == null || agentArgs.trim().isEmpty()) return map;

        String[] parts = agentArgs.split("[,;]");
        for (String rawPart : parts) {
            String part = rawPart.trim();
            if (part.isEmpty()) continue;

            String[] kv = part.split(":", 2);
            if (kv.length == 1) {
                map.put(kv[0].trim(), "true");
            } else {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

}
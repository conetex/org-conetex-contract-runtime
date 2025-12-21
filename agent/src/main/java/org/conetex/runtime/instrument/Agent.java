package org.conetex.runtime.instrument;

import java.lang.instrument.Instrumentation;

// org.conetex.contract.runtime is the right place
public class Agent {

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

    // USAGE: -javaagent:agent/target/agent-0.0.1-SNAPSHOT.jar=pathToTransformerJar:../../metrics-cost/target/metrics-cost-0.0.1-SNAPSHOT.jar
    public static void premain(String agentArgs, Instrumentation inst) {
        Instrument.apply(agentArgs, inst);
    }

}
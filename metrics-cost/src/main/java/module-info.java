module org.conetex.runtime.instrument.metrics {
    requires java.instrument;
    requires org.conetex.runtime.instrument.counter;
    requires org.conetex.runtime.instrument.interfaces;
    requires org.objectweb.asm;
    requires org.objectweb.asm.commons;
    opens org.conetex.runtime.instrument.metrics.cost
            to org.conetex.runtime.instrument;
    exports org.conetex.runtime.instrument.metrics.cost;
}
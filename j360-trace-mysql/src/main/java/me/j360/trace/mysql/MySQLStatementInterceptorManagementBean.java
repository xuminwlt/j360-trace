package me.j360.trace.mysql;


import me.j360.trace.collector.core.ClientTracer;

import java.io.Closeable;
import java.io.IOException;

/**
 * A simple bean whose only purpose in life is to manage the lifecycle of the {@linkplain ClientTracer} in the {@linkplain MySQLStatementInterceptor}.
 */
public class MySQLStatementInterceptorManagementBean implements Closeable {

    public MySQLStatementInterceptorManagementBean(final ClientTracer tracer) {
        MySQLStatementInterceptor.setClientTracer(tracer);
    }

    @Override
    public void close() throws IOException {
        MySQLStatementInterceptor.setClientTracer(null);
    }
}

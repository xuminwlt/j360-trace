package me.j360.trace.dubbo;


/** Well-known {@link BinaryAnnotation#key binary annotation keys} for gRPC */
public final class DubboKeys {

    /**
     * The remote address of the client
     */
    public static final String DUBBO_REMOTE_ADDR = "dubbo.remote_addr";


    public static final String DUBBO_EXCEPTION_NAME = "dubbo.exception_name";

    private DubboKeys() {
    }
}

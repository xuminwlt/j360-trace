package me.j360.trace.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.handu.skye.*;
import com.handu.skye.Tracer;

@Activate(group = {Constants.PROVIDER})
public class J360DubboProviderFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if ("com.alibaba.dubbo.monitor.MonitorService".equals(invoker.getInterface().getName())) {
            return invoker.invoke(invocation);
        }

        RpcContext context = RpcContext.getContext();
        Endpoint endpoint = new Endpoint(context.getLocalHost(), context.getLocalPort());

        String traceId = invocation.getAttachment(Header.TRACE_ID);
        String spanId = invocation.getAttachment(Header.SPAN_ID);
        boolean sampled = Boolean.parseBoolean(invocation.getAttachment(Header.SAMPLED));
        Tracer.setRootSpan(traceId, spanId, sampled);

        // 没有跟踪头不采样
        if (Tracer.lastSpan() == null) {
            return invoker.invoke(invocation);
        }

        Span span = Tracer.begin(context.getMethodName());

        try {
            span.addEvent(Event.SERVER_RECV, endpoint);

            Result result = invoker.invoke(invocation);

            if (result.getException() != null) {
                span.addBinaryEvent("dubbo.exception", result.getException().getMessage(), endpoint);
            }

            return result;
        } catch (RpcException e) {
            span.addBinaryEvent("dubbo.exception", e.getMessage(), endpoint);
            throw e;
        } finally {
            span.addEvent(Event.SERVER_SEND, endpoint);
            Tracer.commit(span);
        }
    }

}

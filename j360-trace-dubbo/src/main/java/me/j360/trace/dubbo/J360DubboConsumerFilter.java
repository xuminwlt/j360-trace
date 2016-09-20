package me.j360.trace.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.handu.skye.*;
import com.handu.skye.Tracer;

@Activate(group = {Constants.CONSUMER})
public class J360DubboConsumerFilter implements Filter {

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        if ("com.alibaba.dubbo.monitor.MonitorService".equals(invoker.getInterface().getName())) {
            return invoker.invoke(invocation);
        }

        RpcContext context = RpcContext.getContext();
        Endpoint endpoint = new Endpoint(context.getLocalHost(), context.getLocalPort());

        Span span = Tracer.begin(context.getMethodName());

        try {
            span.addEvent(Event.CLIENT_SEND, endpoint);
            // 添加下游信息
            ((RpcInvocation) invocation).setAttachment(Header.TRACE_ID, span.getTraceId());
            ((RpcInvocation) invocation).setAttachment(Header.SPAN_ID, span.getId());
            ((RpcInvocation) invocation).setAttachment(Header.SAMPLED, String.valueOf(span.isSampled()));

            Result result = invoker.invoke(invocation);

            if (result.getException() != null) {
                span.addBinaryEvent("dubbo.exception", result.getException().getMessage(), endpoint);
            }

            return result;
        } catch (RpcException e) {
            span.addBinaryEvent("dubbo.exception", e.getMessage(), endpoint);
            throw e;
        } finally {
            span.addEvent(Event.CLIENT_RECV, endpoint);
            Tracer.commit(span);
        }
    }

}

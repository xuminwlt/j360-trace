package me.j360.trace.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import me.j360.trace.collector.core.*;
import me.j360.trace.collector.core.internal.Nullable;
import me.j360.trace.collector.core.module.Endpoint;
import me.j360.trace.http.BraveHttpHeaders;

import java.util.Collection;
import java.util.Collections;

import static com.google.common.base.Preconditions.checkNotNull;

@Activate(group = {Constants.PROVIDER})
public class J360DubboClientFilter implements Filter {

    private final ClientRequestInterceptor clientRequestInterceptor;
    private final ClientResponseInterceptor clientResponseInterceptor;
    private final ClientSpanThreadBinder clientSpanThreadBinder;

    public J360DubboClientFilter(Brave brave) {
        this.clientRequestInterceptor = checkNotNull(brave.clientRequestInterceptor());
        this.clientResponseInterceptor = checkNotNull(brave.clientResponseInterceptor());
        this.clientSpanThreadBinder = checkNotNull(brave.clientSpanThreadBinder());
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        if ("com.alibaba.dubbo.monitor.MonitorService".equals(invoker.getInterface().getName())) {
            return invoker.invoke(invocation);
        }
        RpcContext context = RpcContext.getContext();
        clientRequestInterceptor.handle(new DubboClientRequestAdapter(context, invocation));
        Result result = invoker.invoke(invocation);
        clientResponseInterceptor.handle(new DubboClientResponseAdapter(result));
        return result;
    }


    static final class DubboClientRequestAdapter implements ClientRequestAdapter {

        private final RpcContext context;
        private final Invocation invocation;

        public DubboClientRequestAdapter(RpcContext context,Invocation invocation) {
            this.context = checkNotNull(context);
            this.invocation = checkNotNull(invocation);
        }

        @Override
        public String getSpanName() {
            return context.getMethodName();
        }

        @Override
        public void addSpanIdToRequest(@Nullable SpanId spanId) {

            // 添加下游信息
            if (spanId == null) {
                ((RpcInvocation) invocation).setAttachment(BraveHttpHeaders.Sampled.getName(), "0");
            } else {
                ((RpcInvocation) invocation).setAttachment(BraveHttpHeaders.Sampled.getName(), "1");
                ((RpcInvocation) invocation).setAttachment(BraveHttpHeaders.TraceId.getName(), IdConversion.convertToString(spanId.traceId));
                ((RpcInvocation) invocation).setAttachment(BraveHttpHeaders.SpanId.getName(), IdConversion.convertToString(spanId.spanId));

                if (spanId.nullableParentId() != null) {
                    ((RpcInvocation) invocation).setAttachment(BraveHttpHeaders.ParentSpanId.getName(), IdConversion.convertToString(spanId.parentId));

                }
            }
        }

        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {
            return Collections.emptyList();
        }

        @Override
        public Endpoint serverAddress() {
            return null;
        }
    }

    static final class DubboClientResponseAdapter implements ClientResponseAdapter {

        private final Result result;

        public DubboClientResponseAdapter(Result result) {
            this.result = checkNotNull(result);
        }

        @Override
        public Collection<KeyValueAnnotation> responseAnnotations() {
            return result.getException() == null
                    ? Collections.<KeyValueAnnotation>emptyList()
                    : Collections.singletonList(KeyValueAnnotation.create("dubbo.exception", result.getException().getMessage()));
        }
    }

}

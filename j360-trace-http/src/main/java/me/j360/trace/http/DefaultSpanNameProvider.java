package me.j360.trace.http;


public class DefaultSpanNameProvider implements SpanNameProvider {

    @Override
    public String spanName(HttpRequest request) {
        return request.getHttpMethod();
    }
}

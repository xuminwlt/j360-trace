package me.j360.trace.http;


public interface SpanNameProvider {

    String spanName(HttpRequest request);
}

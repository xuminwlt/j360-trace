package me.j360.trace.http;


import me.j360.trace.collector.core.KeyValueAnnotation;
import me.j360.trace.collector.core.ServerResponseAdapter;
import me.j360.trace.collector.core.TraceKeys;

import java.util.Arrays;
import java.util.Collection;

public class HttpServerResponseAdapter implements ServerResponseAdapter {

    private final HttpResponse response;

    public HttpServerResponseAdapter(HttpResponse response)
    {
        this.response = response;
    }

    @Override
    public Collection<KeyValueAnnotation> responseAnnotations() {
        KeyValueAnnotation statusAnnotation = KeyValueAnnotation.create(
                TraceKeys.HTTP_STATUS_CODE, String.valueOf(response.getHttpStatusCode()));
        return Arrays.asList(statusAnnotation);
    }
}

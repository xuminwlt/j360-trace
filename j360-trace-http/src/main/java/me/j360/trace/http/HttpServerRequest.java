package me.j360.trace.http;



public interface HttpServerRequest extends HttpRequest {

    /**
     * Get http header value.
     *
     * @param headerName
     * @return
     */
    String getHttpHeaderValue(String headerName);

}

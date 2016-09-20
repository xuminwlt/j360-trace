package me.j360.trace.http;


public interface HttpClientRequest extends HttpRequest {

    /**
     * Adds headers to request.
     *
     * @param header header name.
     * @param value header value.
     */
    void addHeader(String header, String value);

}

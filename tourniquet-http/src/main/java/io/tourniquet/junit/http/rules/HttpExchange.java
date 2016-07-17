package io.tourniquet.junit.http.rules;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import io.undertow.util.HeaderMap;
import io.undertow.util.HttpString;
import org.apache.commons.io.IOUtils;

/**
 * Represents a communication between client and server. The exchange provides access to all relevant data received
 * from the client and to write data back to the client.
 */
public class HttpExchange {

    private final HttpServerExchange exchange;
    private byte[] payload;

    HttpExchange(HttpServerExchange exchange) {
        this.exchange = exchange;
    }

    public String getRequestMethod(){
        return this.exchange.getRequestMethod().toString();
    }

    public String getHost() {
        return this.exchange.getHostName();
    }

    public int getPort() {

        return this.exchange.getHostPort();
    }

    public String getHostAndPort() {

        return this.exchange.getHostAndPort();
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(getPayload());
    }

    public byte[] getPayload() {
        if(this.payload == null){
            try {
                this.payload = IOUtils.toByteArray(this.exchange.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.payload;
    }

    public OutputStream getOutputStream() {

        return this.exchange.getOutputStream();
    }

    public String getRequestScheme() {

        return this.exchange.getRequestScheme();
    }

    public String getRequestPath() {

        return this.exchange.getRequestPath();
    }

    public String getRelativePath() {

        return this.exchange.getRelativePath();
    }

    public String getResolvedPath() {

        return this.exchange.getResolvedPath();
    }

    public String getQueryString() {

        return this.exchange.getQueryString();
    }

    public String getRequestURI() {

        return this.exchange.getRequestURI();
    }

    public String getRequestURL() {

        return this.exchange.getRequestURL();
    }

    public Map<String, Deque<String>> getPathParameters() {

        return this.exchange.getPathParameters();
    }

    public Map<String, Deque<String>> getQueryParameters() {

        return this.exchange.getQueryParameters();
    }

    public Map<String, Deque<String>> getRequestHeaders() {

        final HeaderMap headers = this.exchange.getRequestHeaders();
        return headers.getHeaderNames()
                      .stream()
                      .collect(toMap(HttpString::toString,
                                     k -> headers.get(k).stream().collect(toCollection(ArrayDeque::new))));
    }

    public Map<String, String> getRequestCookies() {
        return this.exchange.getRequestCookies().values().stream().collect(toMap(Cookie::getName, Cookie::getValue));

    }

    public long getContentLength() {

        return this.exchange.getRequestContentLength();
    }

    public InetSocketAddress getSourceAddress() {

        return this.exchange.getSourceAddress();
    }

    public InetSocketAddress getDestinationAddress() {

        return this.exchange.getDestinationAddress();
    }

    public void setResponseContentLength(long length) {

        this.exchange.setResponseContentLength(length);
    }

    public void setStatusCode(int code) {

        this.exchange.setStatusCode(code);
    }

    public void addResponseHeader(String name, String value) {

        this.exchange.getResponseHeaders().add(HttpString.tryFromString(name), value);
    }

    public void addResponseCookie(String name, String value) {
        this.exchange.setResponseCookie(new CookieImpl(name, value));
    }

}

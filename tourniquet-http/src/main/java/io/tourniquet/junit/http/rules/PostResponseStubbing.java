package io.tourniquet.junit.http.rules;

import java.util.Map;
import java.util.function.Consumer;

import io.undertow.server.HttpServerExchange;

/**
 * Class for fluently create http POST responses.
 */
public class PostResponseStubbing {

    private final HttpServer server;
    private String path;

    PostResponseStubbing(HttpServer server) {
        this.server = server;
    }

    public PostResponseStubbing respond(String someContent) {
        return this;
    }

    public PostResponseStubbing withParams(Map<String, String> params) {
        return this;
    }

    public PostResponseStubbing withPayload(byte[] data) {
        return this;
    }

    public PostResponseStubbing execute(Consumer<HttpServerExchange> exchangeHandler) {
        return this;
    }
}

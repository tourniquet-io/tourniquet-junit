package io.tourniquet.junit.http.rules;

import io.undertow.server.HttpServerExchange;

/**
 *
 */
public class Exchange {

    private HttpServerExchange exchange;

    public Exchange(HttpServerExchange exchange) {
        this.exchange = exchange;
    }


}

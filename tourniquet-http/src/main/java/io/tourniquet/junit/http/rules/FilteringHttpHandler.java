/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tourniquet.junit.http.rules;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * Handler for HTTP request that dispatches incoming request to matching handler. The request is handled by the first
 * matching handler.
 */
public class FilteringHttpHandler implements HttpHandler {

    private Map<Predicate<HttpExchange>, Consumer<HttpExchange>> handlers = new LinkedHashMap<>();

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            exchange.startBlocking();
            final HttpExchange ex = new HttpExchange(exchange);
            this.handlers.entrySet()
                         .stream()
                         .filter(e -> e.getKey().test(ex))
                         .map(Map.Entry::getValue)
                         .findFirst()
                         .ifPresent(handler -> handler.accept(ex));
        }

    }

    /**
     * Adds a new handler that will receive requests, when specified predicate returns true.
     * @param filter
     *  the filter that activates the handler
     * @param handler
     *  the handler to process the request.
     */
    public void addHandler(Predicate<HttpExchange> filter, Consumer<HttpExchange> handler) {

        this.handlers.put(filter, handler);
    }
}

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

import java.io.IOException;
import java.io.OutputStream;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 * A base class for serving the content of a single resource via Undertow HTTP. <br> This Undertow {@link
 * io.undertow.server.HttpHandler} that dispatches an incoming thread and writes data to the response's output stream.
 * Implementing classes have to implement the {@link #writeResource(java.io.OutputStream)} method in order to write the
 * resource content to the stream.
 */
public abstract class ResourceHttpHandler implements HttpHandler {

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {

        if (exchange.isInIoThread()) {
            exchange.dispatch(this);
        } else {
            exchange.startBlocking();
            writeResource(exchange.getOutputStream());
        }
    }

    /**
     * Writes the resource's content onto the output stream.
     *
     * @param outputStream
     *         the response's outputstream onto which the resource content can be written.
     */
    protected abstract void writeResource(final OutputStream outputStream) throws IOException;
}

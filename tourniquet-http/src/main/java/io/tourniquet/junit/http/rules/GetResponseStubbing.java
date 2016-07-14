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

import static java.nio.charset.Charset.defaultCharset;

import java.util.Optional;

/**
 * Class for fluently create http get responses.
 */
public class GetResponseStubbing {

    private final HttpServer server;
    private String path;
    private String query;

    /**
     * Creates a get response stubbing for the given http server.
     * @param server
     */
    GetResponseStubbing(HttpServer server){
        this.server = server;
    }

    /**
     * Defines the content body of the response
     * @param someContent
     *  the content as string that should be responded
     * @return
     *  this stubbing
     *
     */
    public GetResponseStubbing respond(final String someContent) {
        server.addResource(getPath(), getQuery(), someContent.getBytes(defaultCharset()));
        return this;
    }

    private String getPath() {
        int querySeparator = this.path.indexOf('?');
        if(querySeparator != -1){
            return this.path.substring(0, querySeparator);
        }
        return path;
    }

    private Optional<String> getQuery() {
        int querySeparator = this.path.indexOf('?');
        if(querySeparator != -1){
            return Optional.of(this.path.substring(querySeparator+1));
        }
        return Optional.empty();
    }

    /**
     * Sets the resource that should be requested via GET, as in <pre>
     *     GET /pathToResource HTTP/1.1
     * </pre>
     * @param resource
     *  the path to the resource
     * @return
     *  this stubbing
     */
    GetResponseStubbing resource(final String resource) {
        this.path = resource;
        return this;
    }
}

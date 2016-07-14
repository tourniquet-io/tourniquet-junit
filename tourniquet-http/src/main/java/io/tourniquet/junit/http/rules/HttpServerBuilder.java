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

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.rules.TemporaryFolder;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.net.NetworkUtils;
import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.util.CallStack;
import io.tourniquet.junit.util.ResourceResolver;

/**
 * A Builder for creating an embedded HTTP server. Hostname and tcpPort can be optionally specified and which resources
 * the server should host.
 */
public class HttpServerBuilder implements Builder<HttpServer> {

    private int tcpPort = -1;
    private String serverHostname = "localhost";
    private final ResourceResolver resolver = new ResourceResolver(true);
    private final Map<String, Object> resources = new LinkedHashMap<>();

    @Override
    public HttpServer build() {
        int port = this.tcpPort;
        if(port < 0){
            port = NetworkUtils.findAvailablePort();
        }
        return new HttpServer(serverHostname, port, resources);
    }

    /**
     * Sets the port of the http server. The server will use a random, available port, if no port is specified.
     * @param port
     *  the port the http server will accept incoming requests.
     * @return
     *  this builder
     */
    public HttpServerBuilder port(final int port) {
        this.tcpPort = port;
        return this;
    }

    /**
     * Sets the hostnam for the http server. The server will always run on localhost but may have a different hostname.
     * @param hostname
     *  the hostname of the http server.
     * @return
     *  this builder
     */
    public HttpServerBuilder hostname(final String hostname) {
        this.serverHostname = hostname;
        return this;
    }

    /**
     * Defines a ZIP resource on the classpath that provides the static content the server should host.
     * @param contextRoot
     *  the root path to the content
     * @param contentResource
     *  the name of the classpath resource denoting a file that should be hosted. If the file denotes a zip file, its
     *  content is hosted instead of the file itself.
     *  The path may be absolute or relative to the caller of the method.
     * @return
     *  this builder
     */
    public HttpServerBuilder contentFrom(String contextRoot, String contentResource){
        URL resource = resolver.resolve(contentResource,CallStack.getCallerClass());
        resources.put(contextRoot, resource);
        return this;
    }

    /**
     * Defines a file resource that is dynamically created for the test using the {@link io.tourniquet.junit.rules
     * .TemporaryFile}
     * rule.
     * @param path
     *  the root path to the content
     * @param contentFile
     *  the rule that creates the temporary file that should be hosted by the http server. If the file is a zip
     *  file, it's contents are hosted, not the file itself
     * @return
     *  this builder
     */
    public HttpServerBuilder contentFrom(String path, TemporaryFile contentFile){
        resources.put(path, contentFile);
        return this;
    }

    /**
     * Defines a folder  resource whose content fill be hosted
     * rule.
     * @param contextRoot
     *  the root path to the content
     * @param folder
     *  the rule that creates the temporary folder that should be hosted by the http server.
     * @return
     *  this builder
     */
    public HttpServerBuilder contentFrom(final String contextRoot, final TemporaryFolder folder) {
        resources.put(contextRoot, folder);
        return this;
    }

    /**
     * Defines a file to be hosted on the specified path. The file's content is provided by the specified URL.
     * @param path
     *  the path where the file is accessible from the server
     * @param resource
     *  the resource providing the content for the file
     * @return
     *  this builder
     */
    public HttpServerBuilder contentFrom(final String path, final URL resource) {
        resources.put(path, resource);
        return this;
    }
}

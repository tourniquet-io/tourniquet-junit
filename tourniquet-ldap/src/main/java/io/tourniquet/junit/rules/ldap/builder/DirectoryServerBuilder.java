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

package io.tourniquet.junit.rules.ldap.builder;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.rules.ldap.Directory;
import io.tourniquet.junit.rules.ldap.DirectoryServer;

/**
 * Builder for creating a {@link DirectoryServer} rule. The rule allows to start and stop an embedded ldap server
 * for test.
 * Created by Gerald on 29.05.2015.
 */
public class DirectoryServerBuilder implements Builder<DirectoryServer> {

    /**
     * The {@link Directory} rule that provides the ldap content for the ldap server.
     */
    private final transient Directory directory;

    /**
     * The port the server should accept incoming connections.
     */
    private transient int port = -1;

    /**
     * The listen address for the ldap server.
     */
    private transient String listenAddress;

    /**
     * Flag to indicate the rule should find an available port on each rule application.
     */
    private transient boolean autoBindMode;

    public DirectoryServerBuilder(final Directory directory) {

        this.directory = directory;

    }

    @Override
    public DirectoryServer build() {

        final DirectoryServer directoryServer = new DirectoryServer(this.directory);
        if (this.port != -1) {
            directoryServer.setTcpPort(this.port);
        }
        directoryServer.setListenAddress(this.listenAddress);
        directoryServer.setAutoBind(this.autoBindMode);
        return directoryServer;
    }

    /**
     * Specifies a specific TCP port the ldap server created by the rule should accept incoming connections. If not
     * specified, the default port is 10389.
     *
     * @param port
     *         the port to use for servicing ldap request
     *
     * @return this builder
     */
    public DirectoryServerBuilder onPort(final int port) {

        this.port = port;

        return this;
    }

    /**
     * Specifies the listen address for the ldap server created by the rule. If not specified, localhost is the
     * default.
     *
     * @param address
     *         the listen address for the ldap server
     *
     * @return this builder
     */
    public DirectoryServerBuilder onListenAddress(final String address) {

        this.listenAddress = address;

        return this;
    }

    /**
     * Specifies that the rule should find an available port automatically on each application of the rule.
     *
     * @return this builder
     */
    public DirectoryServerBuilder onAvailablePort() {

        this.autoBindMode = true;

        return this;
    }
}

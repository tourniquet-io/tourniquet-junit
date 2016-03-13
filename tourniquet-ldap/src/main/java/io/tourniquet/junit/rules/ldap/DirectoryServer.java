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

package io.tourniquet.junit.rules.ldap;

import io.tourniquet.junit.rules.ExternalResource;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.ldap.LdapServer;
import org.apache.directory.server.protocol.shared.transport.TcpTransport;
import org.junit.rules.TestRule;

import io.tourniquet.junit.net.NetworkUtils;
import io.tourniquet.junit.rules.RuleSetup;

/**
 * The directory server provides an LDAP service as a {@link TestRule}. It requires a {@link Directory} test rule that
 * contains the LDAP service's content. The server may be configured regarding port and listen address. If neither is
 * configured, it listens on localhost:10389
 */
public class DirectoryServer extends ExternalResource<Directory> {

    /**
     * The server instance managed by this rule.
     */
    private transient LdapServer ldapServer;
    /**
     * the tcp port the server will accept connections.
     */
    private transient int tcpPort = 10389;

    /**
     * The listen address of the server.
     */
    private transient String listenAddress;

    /**
     * Flag indicating the rule is in auto-bind mode. In auto-bind mode, the rule automatically finds an available port
     * on each rule application.
     */
    private transient boolean autoBind;

    public DirectoryServer(final Directory directory) {

        super(directory);
    }

    @Override
    protected void beforeClass() throws Throwable {

        before();
    }

    @Override
    protected void afterClass() {

        after();
    }

    @Override
    protected void before() throws Throwable {

        startServer();
    }

    @Override
    protected void after() {

        shutdownServer();
    }

    /**
     * Shuts down the ldap server. This method is invoked by the apply statement. Override to add additional
     * shutdown behavior.
     */
    protected void shutdownServer() {

        this.ldapServer.stop();

    }

    /**
     * Starts the server on the configured listen address and tcp port (default localhost:10389) and assigns the {@link
     * DirectoryService} provided by the {@link Directory} rule to it.
     *
     * @throws Exception
     *  if the server could not be started for various reasons, i.e. the directory service is not initialized or
     *  the port is already bound.
     */
    protected void startServer() throws Exception { // NOSONAR

        if (this.autoBind) {
            this.setTcpPort(NetworkUtils.findAvailablePort());
        }

        this.ldapServer = new LdapServer();
        this.ldapServer.setDirectoryService(getOuterRule().getDirectoryService());
        this.ldapServer.setTransports(new TcpTransport(this.getTcpPort()));
        this.ldapServer.start();
    }

    /**
     * The tcp port the ldap server listens for incoming connections.
     * @return
     *  the tcp port number
     */
    public int getTcpPort() {

        return this.tcpPort;
    }

    /**
     * Sets the TCP port the LDAP server will listen on for incoming connections. The port must be set before
     * the rule is applied.
     * @param tcpPort
     *  the tcp port number
     */
    @RuleSetup
    public void setTcpPort(final int tcpPort) {

        this.tcpPort = tcpPort;
    }

    /**
     * Provides access to the {@link org.apache.directory.server.ldap.LdapServer}.
     *
     * @return
     *  the LdapServer used by this rule
     */
    public LdapServer getLdapServer() {

        return this.ldapServer;
    }

    /**
     * @return the listen address of the ldap server.
     */
    public String getListenAddress() {

        return this.listenAddress;
    }

    /**
     * Sets the listen address of the server. If none is set, localhost (null) is used.
     * @param listenAddress
     *  the new listen address of the server
     */
    @RuleSetup
    public void setListenAddress(final String listenAddress) {

        this.listenAddress = listenAddress;
    }

    /**
     * Sets the rule to auto-bind, that will find an available port on each application. The port may be
     * access using the {@code getTcpPort()} method.
     * @param autoBind
     *  <code>true</code> to activate the auto-bind mode
     */
    @RuleSetup
    public void setAutoBind(final boolean autoBind) {

        this.autoBind = autoBind;
    }

    /**
     * The directory service manages the entries provided by the LdapServer.
     *
     * @return the DirectoryService to access the entries of the LDAP server directly
     */
    public DirectoryService getDirectoryService() {

        return getOuterRule().getDirectoryService();
    }
}

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

/**
 *
 */
package io.tourniquet.junit.jcr.rules;

import javax.jcr.Repository;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import java.io.IOException;
import java.net.URL;
import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A test rule for accessing a JCR repository remotely. The precondition for this rule is, that the Jackrabbit WebApp is
 * deployed on the remote server as this rules connects to the servlets (WebDav) contained in the web app. The intention
 * for this rule is to set up an existing and running repository for the test, i.e. to load content for an integration
 * or acceptance test. When being used with arquillian, this rule may to be used as static {@link ClassRule}. The
 * initialize method has to be invoked in a {@link BeforeClass} annotated class if the Jackrabbit web is being deployed
 * as part of the arquillian deployment.
 *
 */
public class RemoteContentRepository extends ContentRepository {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(RemoteContentRepository.class);
    /**
     * The name of the host on which the jackrabbit web app is installed
     */
    private String remoteHost = "localhost";
    /**
     * The port of the web container that hosts the jackrabbit web app
     */
    private int remotePort = 8080;
    /**
     * The context root that points to the jackrabbit webapp
     */
    private String contextRoot = "/jackrabbit-webapp-2.8.0/server";
    private boolean manualSetup;
    private URL arquillianXml;

    public RemoteContentRepository() {

        super(null);
    }

    /**
     * Overrides the default host name for the remote content repository. The default is 'localhost'
     *
     * @param hostname
     *         the new hostname to use
     *
     * @return this test rule
     */
    public RemoteContentRepository onHost(final String hostname) {

        remoteHost = hostname;
        return this;
    }

    /**
     * Overrides the default host port of the remote content repository. The default is '8080'
     *
     * @param port
     *         the new port to use
     *
     * @return this test rule
     */
    public RemoteContentRepository onPort(final int port) {

        remotePort = port;
        return this;
    }

    /**
     * Overrides the default context root of the jackrabbit web app. The default is '/jackrabbit-webapp-2.8.0/server'.
     *
     * @param contextRoot
     *         the new context root to use
     *
     * @return this test rule
     */
    public RemoteContentRepository atContextRoot(final String contextRoot) {

        this.contextRoot = contextRoot;
        return this;
    }

    /**
     * If this method is invoked, the rule will be configured to not perform the lookup when being evaluated. Instead
     * the initialize method has to be invoked in the {@link BeforeClass} or {@link Before} annotated method.
     *
     * @return this test rule
     */
    public RemoteContentRepository setupManually() {

        manualSetup = true;
        return this;
    }

    /**
     * Indicates to use the same host as defined in the aquillian.xml profiles. The active profile is either determined
     * by the system property <code>arquillian.launch</code> or the default is used. If the arquillian.xml file is
     * <code>null</code> the default of the rule is used (localhost)
     *
     * @param arquillianXml
     *         the URL to the arquillian.xml file containing the arquillian profiles
     *
     * @return this test rule
     */
    public RemoteContentRepository onArquillianHost(final URL arquillianXml) {

        this.arquillianXml = arquillianXml;
        return this;
    }

    @Override
    protected void before() throws Throwable {

        if (arquillianXml != null) {
            setupHostFromArquillianConfig(arquillianXml);
        }

        if (!manualSetup) {
            super.before();
        }
    }

    /**
     * Obtains the hostname from the active arquillian launch configuration. The configuration is set by the
     * <code>arquillian.launch</code> system property. If it is not set, the default will be used.
     *
     * @param arquillianXml
     *         the URL to the arquillian xml file
     */
    private void setupHostFromArquillianConfig(final URL arquillianXml) {

        final String arquillianLaunch = System.getProperty("arquillian.launch");

        if (arquillianLaunch == null) {
            LOG.warn("No launch configuration specified, using default");

        } else {
            final String arquillianHost = getActiveArquillianHost(arquillianXml, arquillianLaunch);
            if (arquillianHost != null) {
                remoteHost = arquillianHost;
            }
        }

    }

    /**
     * Reads the active remote host from the <code>arquillian.xml</code>. The host is activated by the defining
     * container which is activated by its qualified using the <code>arquillian.launch</code> system property. If there
     * is no host defined, <code>null</code> is returned indicating to use the default configuration.
     *
     * @param arquillianXml
     *         the URL of the arquillian.xml file
     * @param arquillianLaunch
     *         the launch container qualifier. The qualifier may consist of alphanumeric characters only.
     *
     * @return the name of the remote host or <code>null</code> if none is defined
     */
    private String getActiveArquillianHost(final URL arquillianXml, final String arquillianLaunch) {

        final XPath xPath = XPathFactory.newInstance().newXPath();
        final String xpExpr = "//container[@qualifier=$containerQualifier]/protocol/property[@name='host']";
        try {
            final Document document = parseDocument(arquillianXml);

            //SCRIB-23 adding a variable resolver that returns only sanitized input values
            xPath.setXPathVariableResolver(new ArquillianLaunchVariableResolver(arquillianLaunch));
            // with the sanity check the already unlikely chance of maliciously injecting statements into
            // the xpath expression becomes impossible. As the sonar rule still indicating this as
            // an error, the NOSONAR marker is set
            return xPath.evaluate(xpExpr, document);//NOSONAR
        } catch (SAXException | IOException | ParserConfigurationException e) {
            LOG.error("Could not parse arquilian.xml", e);
            throw new AssertionError("Could not parse arquilian.xml:" + e.getMessage(), e);
        } catch (final XPathExpressionException e) {
            LOG.error("Could evaluate {}", xpExpr, e);
            throw new AssertionError("Could evaluate " + xpExpr + ":" + e.getMessage(), e);
        }
    }

    private Document parseDocument(final URL documentLocation)
            throws SAXException, IOException, ParserConfigurationException {

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // SCRIB-24 adding secure parsing to prevent XEE attacks
        //but as the arquillian.xml files are bound to a xsd schema we don't need doctype declaration and can
        //safely disable this feature
        //for more info see https://www.owasp.org/index.php/XML_External_Entity_%28XXE%29_Processing
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        return factory.newDocumentBuilder().parse(documentLocation.openStream());
    }

    @Override
    protected void after() {

        if (!manualSetup) {
            super.after();
        }
    }

    @Override
    protected Repository createRepository() throws Exception {

        final URL remoteUrl = new URL("http", remoteHost, remotePort, contextRoot);
        LOG.info("Creating remote repository for {}", remoteUrl);

        return JcrUtils.getRepository(remoteUrl.toString());
    }

    @Override
    protected void destroyRepository() { // NOSONAR

    }

    protected String getRemoteHost() {

        return remoteHost;
    }

    protected int getRemotePort() {

        return remotePort;
    }

    protected String getContextRoot() {

        return contextRoot;
    }

    private static class ArquillianLaunchVariableResolver implements XPathVariableResolver {

        private final String qualifier;

        public ArquillianLaunchVariableResolver(String arquillianLaunchParameter) {
            //SCRIB-23 doing parameter sanity check
            if (!arquillianLaunchParameter.matches("[a-zA-Z0-9]+")) {
                throw new AssertionError(arquillianLaunchParameter + " is no allowed qualifier");
            }
            qualifier = arquillianLaunchParameter;
        }

        @Override
        public Object resolveVariable(final QName variableName) {

            if ("containerQualifier".equals(variableName.getLocalPart())) {
                return qualifier;
            }
            return "";
        }
    }
}

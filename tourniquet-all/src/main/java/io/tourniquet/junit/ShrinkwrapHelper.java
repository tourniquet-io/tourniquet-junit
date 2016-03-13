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

package io.tourniquet.junit;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.Testable;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.PomEquippedResolveStage;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

/**
 * Helper class for creating archives for Arquillian based tests.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class ShrinkwrapHelper {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ShrinkwrapHelper.class);

    private ShrinkwrapHelper() {

    }

    /**
     * Creates an enterprise archive for integration testing that contains the specified archiveToTest the enterprise
     * archive contains other modules that are required to run the integration test. Those modules are
     * <ul>
     * <li>Jackrabbit WebApp (DAV Servlet)</li>
     * </ul>
     *
     * @param archiveToTest
     *            the archive containing the resources under test
     * @return the enterprise archive to de deployed on the jEE container
     */
    @Deployment
    public static EnterpriseArchive createIntegrationTestDeployment(final WebArchive archiveToTest) {

        final EnterpriseArchive ear = createEARDeployment();
        archiveToTest.addClass(ShrinkwrapHelper.class);
        ear.addAsModule(Testable.archiveToTest(archiveToTest));
        return ear;
    }

    /**
     * Creates an enterprise archive for the module in whose working directory the method is invoked, including all
     * compile-scoped jar archives.
     *
     * @return an enterprise containgin all dependency of the current module
     */
    public static EnterpriseArchive createEARDeployment() {

        final EnterpriseArchive ear = ShrinkWrap.create(EnterpriseArchive.class, "tourniquet-test.ear");

        // use local maven repository mirror only
        final PomEquippedResolveStage pom = Maven.configureResolver().workOffline().loadPomFromFile("pom.xml");

        final File[] files = pom.importDependencies(ScopeType.COMPILE).resolve().withTransitivity().asFile();
        for (final File f : files) {
            if (f.getName().endsWith(".jar")) {
                LOG.debug("Adding lib {}", f);
                ear.addAsLibrary(f);
            }
        }
        return ear;
    }

    /**
     * Creates a Jackrabbit WebArchive for a CDI 1.0 container that may be deployed along with the web app under test.
     * The jackrabbit web app uses a customized web.xml to provide a repository that is available as JNDI resource.
     *
     * @return
     */
    public static WebArchive createJackrabbitCDI10Webapp() {

        // extract the jackrabbit webapp
        final File jackrabbitWarFile = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.jackrabbit:jackrabbit-webapp:war:?").withTransitivity().asSingleFile();
        // obtain the CDI1.0 compatible guava 15 jar
        final File guava15CDIJarFile = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("com.google.guava:guava:jar:cdi1.0:15.0").withTransitivity().asSingleFile();

        // get the custom web.xml
        final URL webXml = ShrinkwrapHelper.class.getResource("jackrabbit_webapp_web.xml");

        // create a patched war file with the custom web.xml and the guava jar
        final WebArchive jackrabbitWar = ShrinkWrap.createFromZipFile(WebArchive.class, jackrabbitWarFile)
                .setWebXML(webXml).addAsLibraries(guava15CDIJarFile);
        // delete the incompatible guava jar
        jackrabbitWar.delete("/WEB-INF/lib/guava-15.0.jar");
        return jackrabbitWar;
    }

    /**
     * Obtains a resource locator to a resource inside the classpath of the integration test
     *
     * @param resourceName
     *            the name of the resource to retrieve
     * @return the URL to the resource or <code>null</code> if it is not found
     */
    public static URL getResource(final String resourceName) {

        return ShrinkwrapHelper.class.getResource(resourceName);
    }
}

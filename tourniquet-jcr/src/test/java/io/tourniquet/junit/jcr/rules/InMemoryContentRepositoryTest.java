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

package io.tourniquet.junit.jcr.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.net.URL;

import io.tourniquet.junit.rules.BaseRule;
import io.tourniquet.junit.rules.BaseRuleHelper;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.config.VersioningConfig;
import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.fs.mem.MemoryFileSystem;
import org.apache.jackrabbit.core.persistence.mem.InMemBundlePersistenceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryContentRepositoryTest {
    @Rule
    public final TemporaryFolder workingDirectory = new TemporaryFolder();

    private InMemoryContentRepository subject;

    @Before
    public void setUp() throws Exception {
        subject = new InMemoryContentRepository(workingDirectory);
    }

    @After
    public void tearDown() throws Exception {
    }


    @Test
    public void testGetConfigUrl() throws Exception {
        final URL configUrl = subject.getConfigUrl();

        // assert
        assertNotNull(configUrl);
    }

    /**
     * Verifies the persistence parts of the configuration use in-memory components.
     *
     * @throws Throwable
     */
    @Test
    public void testInMemoryConfiguraton() throws Throwable {
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        final RepositoryConfig config = subject.createRepositoryConfiguration();

        assertTrue(config.getFileSystem() instanceof MemoryFileSystem);

        final VersioningConfig vconfig = config.getVersioningConfig();
        assertTrue(vconfig.getFileSystem() instanceof MemoryFileSystem);
        assertEquals(InMemBundlePersistenceManager.class.getName(), vconfig.getPersistenceManagerConfig()
                .getClassName());

        final WorkspaceConfig wsconfig = config.getWorkspaceConfig("default");
        assertTrue(wsconfig.getFileSystem() instanceof MemoryFileSystem);
        assertEquals(InMemBundlePersistenceManager.class.getName(), wsconfig.getPersistenceManagerConfig()
                .getClassName());

    }


    @Test
    public void testSetGetNodeTypeDefinitions() throws Exception {
        //prepare
        URL someUrl = new URL("http://localhost");

        //act
        subject.setCndUrl(someUrl);
        URL actualUrl = subject.getCndUrl();

        //assert
        assertEquals(someUrl, actualUrl);

    }

    @Test
    public void testGetAdminSession() throws Throwable {
        //prepare
        subject.before();

        //act
        Session session = subject.getAdminSession();

        //assert
        assertNotNull(session);
        assertEquals("admin", session.getUserID());
        AccessControlManager acm = session.getAccessControlManager();
        assertTrue(acm.hasPrivileges("/", new Privilege[] { acm.privilegeFromName(Privilege.JCR_ALL) }));

    }
}

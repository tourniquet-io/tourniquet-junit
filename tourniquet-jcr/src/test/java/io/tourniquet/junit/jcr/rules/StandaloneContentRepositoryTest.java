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
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.net.URL;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.tourniquet.junit.rules.BaseRule;
import io.tourniquet.junit.rules.BaseRuleHelper;

public class StandaloneContentRepositoryTest {

    private final URL configUrl = getClass().getResource("StandaloneContentRepositoryTest_repository.xml");
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private StandaloneContentRepository subject;

    private RepositoryImpl repositorySpy;

    @Before
    public void setUp() throws Exception {
        subject = new StandaloneContentRepository(folder) {
            @Override
            public Repository getRepository() {
                repositorySpy = spy((RepositoryImpl) super.getRepository());
                return repositorySpy;
            }
        };
    }

    @After
    public void tearDown() throws Exception {

        subject.after();
    }

    @Test
    public void testCreateRepository_noConfigUrl_useDefaultConfig() throws Exception {
        // act
        final Repository repository = subject.createRepository();

        // assert
        assertNotNull(repository);
    }

    @Test
    public void testCreateRepository_withConfigUrl() throws Exception {
        // prepare
        subject.setConfigUrl(configUrl);

        // act
        final Repository repository = subject.createRepository();

        // assert
        assertNotNull(repository);

    }

    @Test
    public void testDestroyRepository() throws Throwable {
        // prepare
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // act
        subject.destroyRepository();
        // assert
        assertNotNull(repositorySpy);
        verify(repositorySpy).shutdown();
    }

    @Test
    public void testSetGetConfigUrl() throws Exception {
        //prepare

        URL someUrl = new URL("http://localhost");

        //act
        subject.setConfigUrl(someUrl);
        URL actualUrl = subject.getConfigUrl();

        //assert
        assertEquals(someUrl, actualUrl);

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

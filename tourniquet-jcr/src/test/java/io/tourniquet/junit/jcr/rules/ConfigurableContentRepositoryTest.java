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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.net.URL;

import io.tourniquet.junit.rules.BaseRule;
import io.tourniquet.junit.rules.BaseRuleHelper;
import io.tourniquet.junit.jcr.JCRAssert;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurableContentRepositoryTest {

    private final URL configUrl = getClass().getResource("ConfigurableContentRepositoryTest_repository.xml");
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    @Mock
    private Repository repository;
    private ConfigurableContentRepository subject;

    @Before
    public void setUp() throws Exception {

        subject = new ConfigurableContentRepository(folder) {
            @Override
            protected void destroyRepository() {

            }

            @Override
            protected Repository createRepository() throws Exception {

                return repository;
            }

        };
    }

    @After
    public void tearDown() throws Exception {

        if (repository instanceof RepositoryImpl) {
            ((RepositoryImpl) repository).shutdown();
        }
    }

    @Test
    public void testCreateRepositoryConfiguration() throws Throwable {
        // prepare
        subject.setConfigUrl(configUrl);
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        // act
        final RepositoryConfig config = subject.createRepositoryConfiguration();

        // assert
        assertNotNull(config);
        assertEquals(folder.getRoot().getAbsolutePath(), config.getHomeDir());
    }

    @Test(expected = AssertionError.class)
    public void testCreateRepositoryConfiguration_noConfiguration_fail() throws Throwable {
        // prepare
        subject.setConfigUrl(null);
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        // act
        subject.createRepositoryConfiguration();
    }

    @Test
    public void testSetGetConfigUrl_beforeStateInitialized_ok() throws Exception {

        subject.setConfigUrl(configUrl);
        assertEquals(configUrl, subject.getConfigUrl());
    }

    @Test(expected = AssertionError.class)
    public void testSetGetConfigUrl_afterStateInitialized_fail() throws Exception {

        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        //act
        subject.setConfigUrl(configUrl);
    }

    @Test
    public void testSetGetCndUrl_beforeStateInitialized_ok() throws Exception {

        //prepare
        URL cndUrl = new URL("http://localhost");

        //act
        subject.setCndUrl(cndUrl);

        //assert
        assertEquals(cndUrl, subject.getCndUrl());
    }

    @Test(expected = AssertionError.class)
    public void testSetGetCndUrl_afterStateInitialized_fail() throws Exception {

        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        //act
        subject.setCndUrl(configUrl);
    }

    @Test
    public void testInitialized() throws Throwable {
        //prepare
        final URL cndResource = getClass().getResource("ConfigurableContentRepositoryTest_testModel.cnd");
        subject.setConfigUrl(configUrl);
        subject.setCndUrl(cndResource);
        //create real repository for this test and override the mock instance
        repository = RepositoryImpl.create(subject.createRepositoryConfiguration());

        ConfigurableContentRepository spy = spy(subject);

        //act
        //invoke beforeClass which _should_ invoke initialize, we'll verify later
        spy.beforeClass();

        //assert
        verify(spy).initialize();
        final Session session = repository.login();
        JCRAssert.assertNodeTypeExists(session, "test:testType");
        session.logout();
    }

    @Test(expected = AssertionError.class)
    public void testInitialized_invalidCndUrl_fail() throws Throwable {
        //prepare
        URL cndResource = new URL("file:///notexisting");
        subject.setConfigUrl(configUrl);
        subject.setCndUrl(cndResource);
        //create real repository for this test and override the mock instance
        repository = RepositoryImpl.create(subject.createRepositoryConfiguration());

        //act
        //invoke beforeClass which _should_ invoke initialize, we'll verify later
        subject.beforeClass();
    }

    @Test(expected = AssertionError.class)
    public void testInitialized_repositoryError_fail() throws Throwable {
        //prepare
        final URL cndResource = getClass().getResource("ConfigurableContentRepositoryTest_testModel.cnd");
        subject.setConfigUrl(configUrl);
        subject.setCndUrl(cndResource);
        //force an error on loggin in to the repository
        when(repository.login(any(Credentials.class))).thenThrow(RepositoryException.class);

        //act
        //invoke beforeClass which _should_ invoke initialize, we'll verify later
        subject.beforeClass();
    }

    @Test(expected = AssertionError.class)
    public void testInitialized_invalidCnd_fail() throws Throwable {
        //prepare
        final URL cndResource = getClass().getResource("ConfigurableContentRepositoryTest_invalidTestModel.cnd");
        subject.setConfigUrl(configUrl);
        subject.setCndUrl(cndResource);
        //force an error on loggin in to the repository
        repository = RepositoryImpl.create(subject.createRepositoryConfiguration());

        //act
        //invoke beforeClass which _should_ invoke initialize, we'll verify later
        subject.beforeClass();
    }



}

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.AccessDeniedException;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import io.tourniquet.junit.rules.BaseRule;
import io.tourniquet.junit.rules.BaseRuleHelper;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ContentRepositoryTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private Repository repository;

    @Mock
    private Session session;

    private ContentRepository subject;

    private AtomicBoolean exceptionInInitialize = new AtomicBoolean(false);

    @Before
    public void setUp() throws Exception {

        subject = new ContentRepository(folder) {

            private URL nodeTypeDefinitions;

            public void setNodeTypeDefinitions(final URL nodeTypeDefinitions) {

                this.nodeTypeDefinitions = nodeTypeDefinitions;
            }

            @Override
            protected void destroyRepository() {

            }

            @Override
            protected Repository createRepository() throws Exception {

                return repository;
            }

            @Override
            protected void initialize() {

                if (exceptionInInitialize.get()) {
                    throw new RuntimeException();
                }
            }
        };
    }

    @Test
    public void testBeforeClass() throws Throwable {
        // act
        subject.beforeClass();

        // assert
        assertNotNull(subject.getRepository());
        Assert.assertEquals(BaseRule.State.INITIALIZED, BaseRuleHelper.getState(subject));
    }

    @Test
    public void testAfterClass() throws Throwable {
        // prepare
        final ContentRepository spy = spy(subject);
        spy.beforeClass();
        // act
        spy.afterClass();
        // assert
        verify(spy).destroyRepository();
        assertEquals(BaseRule.State.DESTROYED, BaseRuleHelper.getState(spy));
    }

    @Test
    public void testBefore_alreadyCreated_noSetup() throws Throwable {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.CREATED);

        //act
        subject.before();

        //assert
        assertEquals(BaseRule.State.CREATED, BaseRuleHelper.getState(subject));
    }

    @Test
    public void testBefore_new_setup() throws Throwable {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.NEW);

        //act
        subject.before();

        //assert
        assertEquals(BaseRule.State.BEFORE_EXECUTED, BaseRuleHelper.getState(subject));
    }

    @Test
    public void testBefore_exceptionInInitialization() throws Throwable {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.NEW);
        this.exceptionInInitialize.set(true);

        //act
        try {
            subject.before();
        } catch (RuntimeException e) {
            //this was expected
        }

        //assert
        assertEquals(BaseRule.State.CREATED, BaseRuleHelper.getState(subject));
    }

    @Test
    public void testAfter_notBeforeExecuted_noTeardown() throws Exception {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.CREATED);

        //act
        subject.after();

        //assert
        assertEquals(BaseRule.State.CREATED, BaseRuleHelper.getState(subject));
    }

    @Test
    public void testAfter_beforeExecuted_teardown() throws Exception {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.BEFORE_EXECUTED);

        //act
        subject.after();

        //assert
        assertEquals(BaseRule.State.AFTER_EXECUTED, BaseRuleHelper.getState(subject));
    }

    @Test
    public void testAfter_beforeExecuted_and_activeAdminSession_teardownAndLogout() throws Throwable {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.NEW);
        subject.before();
        when(repository.login(any(SimpleCredentials.class))).thenReturn(session);
        when(session.isLive()).thenReturn(true);
        subject.getAdminSession();

        //act
        subject.after();

        //assert
        assertEquals(BaseRule.State.AFTER_EXECUTED, BaseRuleHelper.getState(subject));
        verify(session).logout();
    }

    @Test
    public void testAfter_beforeExecuted_and_activeAdminSession_teardownAndNoLogout() throws Throwable {
        //prepare
        BaseRuleHelper.setState(subject, BaseRule.State.NEW);
        subject.before();
        when(repository.login(any(SimpleCredentials.class))).thenReturn(session);
        when(session.isLive()).thenReturn(false);
        subject.getAdminSession();

        //act
        subject.after();

        //assert
        assertEquals(BaseRule.State.AFTER_EXECUTED, BaseRuleHelper.getState(subject));
        verify(session, times(0)).logout();
    }

    @Test(expected = AssertionError.class)
    public void testGetRepository_uninitialized_fail() throws Exception {

        subject.getRepository();
    }

    @Test
    public void testGetRepository_initialized_ok() throws Throwable {
        // prepare
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // act
        final Repository repo = subject.getRepository();
        // assert
        assertEquals(repository, repo);
    }

    @Test(expected = AssertionError.class)
    public void testGetInjectionValue_uninitialized_fail() throws Exception {

        subject.getInjectionValue();
    }

    @Test
    public void testGetInjectionValue_initialized_ok() throws Throwable {
        // prepare
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // act
        final Repository repo = subject.getInjectionValue();
        // assert
        assertEquals(repository, repo);
    }

    @Test
    public void testGetWorkingDirectory_initialized_ok() throws Throwable {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // act
        final TemporaryFolder workingDirectory = subject.getWorkingDirectory();
        // aasert
        assertEquals(folder, workingDirectory);
    }

    @Test(expected = AssertionError.class)
    public void testGetWorkingDirectory_notInitialized_fail() throws Throwable {

        subject.getWorkingDirectory();
    }

    @Test(expected = AssertionError.class)
    public void testLogin_notInitialized_fail() throws Throwable {

        subject.login("aUser", "aPassword");
    }

    @Test
    public void testLogin_initialized() throws Throwable {
        // prepare
        when(repository.login(any(SimpleCredentials.class))).thenReturn(session);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // act
        final Session userSession = subject.login("aUser", "aPassword");
        // assert
        assertNotNull(userSession);
        assertEquals(session, userSession);

        final ArgumentCaptor<SimpleCredentials> captor = ArgumentCaptor.forClass(SimpleCredentials.class);
        verify(repository).login(captor.capture());
        final SimpleCredentials passedParam = captor.getValue();
        assertEquals("aUser", passedParam.getUserID());
        assertEquals("aPassword", String.valueOf(passedParam.getPassword()));
    }

    @Test
    public void testGetAdminSession_firstLogin() throws Throwable {

        //prepare
        when(repository.login(any(SimpleCredentials.class))).thenReturn(session);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        // act
        final Session adminSession = subject.getAdminSession();

        // assert
        assertNotNull(adminSession);
        assertEquals(session, adminSession);

        //verify an admin login has been performed
        final ArgumentCaptor<SimpleCredentials> captor = ArgumentCaptor.forClass(SimpleCredentials.class);
        verify(repository).login(captor.capture());
        final SimpleCredentials passedParam = captor.getValue();
        assertEquals("admin", passedParam.getUserID());
        assertEquals("admin", String.valueOf(passedParam.getPassword()));
    }

    @Test
    public void testGetAdminSession_consecutiveLogin() throws Throwable {

        //prepare
        when(repository.login(any(SimpleCredentials.class))).thenReturn(session);
        when(session.isLive()).thenReturn(true);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        //the first login
        subject.getAdminSession();

        // act
        //the second login
        Session adminSession = subject.getAdminSession();

        // assert
        assertNotNull(adminSession);
        assertEquals(session, adminSession);

        //verify no new admin login has been performed, only 1 login from the prepare phase
        verify(repository, times(1)).login(any(SimpleCredentials.class));
        //and the session was refreshed
        verify(session).refresh(false);
    }

    @Test
    public void testGetAdminSession_inactiveSession() throws Throwable {

        //prepare
        when(repository.login(any(SimpleCredentials.class))).thenReturn(session);
        when(session.isLive()).thenReturn(false);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        //the first login
        subject.getAdminSession();

        // act
        //the second login
        Session adminSession = subject.getAdminSession();

        // assert
        assertNotNull(adminSession);
        assertEquals(session, adminSession);

        //verify an new login has been performed
        final ArgumentCaptor<SimpleCredentials> captor = ArgumentCaptor.forClass(SimpleCredentials.class);
        //two logins are performed, 1 in prepare and 1 in act
        verify(repository, times(2)).login(captor.capture());
        final SimpleCredentials passedParam = captor.getValue();
        assertEquals("admin", passedParam.getUserID());
        assertEquals("admin", String.valueOf(passedParam.getPassword()));
    }

    @Test
    public void testLogin_anonymous_firstLogin() throws Throwable {

        //prepare
        when(repository.login()).thenReturn(session);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);

        // act
        final Session session = subject.login();

        // assert
        assertNotNull(session);
        assertEquals(this.session, session);
    }

    @Test
    public void testLogin_anonymous_consecutiveLogin() throws Throwable {

        //prepare
        when(repository.login()).thenReturn(session);
        when(session.isLive()).thenReturn(true);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        //the first login
        subject.login();

        // act
        //the second login
        Session session = subject.login();

        // assert
        assertNotNull(session);
        assertEquals(this.session, session);

        //verify no new admin login has been performed, only 1 login from the prepare phase
        verify(repository, times(1)).login();
        //and the session was refreshed
        verify(this.session).refresh(false);
    }

    @Test
    public void testLogin_anonymous_inactiveSession() throws Throwable {

        //prepare
        when(repository.login()).thenReturn(session);
        when(session.isLive()).thenReturn(false);
        subject.before();
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        //the first login
        subject.login();

        // act
        //the second login
        Session session = subject.login();

        // assert
        assertNotNull(session);
        assertEquals(this.session, session);

        //two logins are performed, 1 in prepare and 1 in act
        verify(repository, times(2)).login();
    }

    @Test
    public void testGrant() throws Throwable {
        //prepare
        setupInMemoryRepository();

        //act
        subject.grant("anonymous", "/", "jcr:all");

        //assert
        assertNotNull(repository.login().getRootNode().addNode("test", "nt:unstructured"));

    }

    /**
     * Replaces the mock repository with a real repository with effective security and in-memory persistence.
     *
     * @throws Throwable
     */
    private void setupInMemoryRepository() throws Throwable {

        final URL configUrl = getClass().getResource("ContentRepositoryTest_repository.xml");
        RepositoryConfig config = RepositoryConfig.create(configUrl.toURI(), folder.getRoot().getAbsolutePath());
        this.repository = RepositoryImpl.create(config);
        subject.before();
    }

    @Test(expected = AccessDeniedException.class)
    public void testClearACLs_matchingSingleUser() throws Throwable {
        //prepare
        setupInMemoryRepository();
        //see testGrant that this works
        subject.grant("anonymous", "/", "jcr:all");

        //act
        subject.clearACL("/", "anonymous");

        //assert
        //this call will fail as the removed the all permission
        repository.login().getRootNode().addNode("test", "nt:unstructured");
    }

    @Test
    public void testClearACLs_notMatchingSingleUser() throws Throwable {
        //prepare
        setupInMemoryRepository();
        //see testGrant that this works
        subject.grant("anonymous", "/", "jcr:all");

        //act
        subject.clearACL("/", "someoneElse");

        //assert
        assertNotNull(repository.login().getRootNode().addNode("test", "nt:unstructured"));
    }

    @Test(expected = LoginException.class)
    public void testClearACLs_allUsers() throws Throwable {
        //prepare
        setupInMemoryRepository();
        //see testGrant that this works
        subject.grant("anonymous", "/", "jcr:all");

        //act
        subject.clearACL("/");

        //assert
        //this call will fail as the removed the all permission on the root node so that no user can login anymore
        repository.login();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeny() throws Exception {
        //prepare

        //act
        subject.deny("anyId", "anyPath", "jcr:all");

        //assert

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testAddUser() throws Exception {
        //prepare

        //act
        subject.addUser("anyName", "anyPassword");

        //assert

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDeleteUser() throws Exception {
        //prepare

        //act
        subject.deleteUser("anyName");

        //assert

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testResetUsers() throws Exception {
        //prepare

        //act
        subject.resetUsers();

        //assert

    }
}

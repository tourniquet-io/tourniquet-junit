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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.Credentials;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import io.tourniquet.junit.rules.BaseRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.tourniquet.junit.rules.BaseRuleHelper;

@RunWith(MockitoJUnitRunner.class)
public class ActiveSessionTest {

    @Mock
    private Session adminSession;

    @Mock
    private Session anonSession;

    @Mock
    private Session userSession;

    @Mock
    private Repository repository;

    @Mock
    private ContentRepository repositoryRule;
    private ActiveSession subject;

    @Before
    public void setUp() throws Exception {
        subject = new ActiveSession(repositoryRule);
        when(repositoryRule.getRepository()).thenReturn(repository);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAfter_withActiveAnonAndUserAndAdminSession() throws Exception {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login()).thenReturn(anonSession);
        when(repository.login(any(Credentials.class))).thenReturn(userSession, adminSession);
        subject.login();
        subject.login("user1", "password");
        subject.getAdminSession();
        // act
        subject.after();
        // assert
        verify(anonSession).logout();
        verify(userSession).logout();
        verify(adminSession).logout();

    }

    @Test
    public void testAfter_withActiveAdminSession() throws Exception {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login(any(Credentials.class))).thenReturn(adminSession);
        subject.getAdminSession();
        // act
        subject.after();
        // assert
        verify(adminSession).logout();
    }

    @Test
    public void testAfter_withActiveUserSession() throws Exception {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login(any(Credentials.class))).thenReturn(userSession);
        subject.login("user", "password");
        // act
        subject.after();
        // assert
        verify(userSession).logout();
    }

    @Test
    public void testAfter_withActiveAnonSession() throws Exception {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login()).thenReturn(anonSession);
        subject.login();
        // act
        subject.after();
        // assert
        verify(anonSession).logout();
    }

    @Test
    public void testGetRepository_initialized() throws Throwable {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // act
        assertEquals(repository, subject.getRepository());
    }

    @Test(expected = AssertionError.class)
    public void testGetRepository_uninitialized() throws Exception {
        subject.getRepository();
    }

    @Test
    public void testLogin_initialized_noUserName() throws Throwable {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login()).thenReturn(anonSession);
        // act
        final Session session = subject.login();
        // assert
        assertNotNull(session);
        assertEquals(anonSession, session);
    }

    @Test
    public void testLogin_initialized_repeatedLogin() throws Throwable {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        // invoked twice on the second call will create a new session
        when(repository.login()).thenReturn(anonSession, mock(Session.class));
        // act
        final Session session1 = subject.login();
        final Session session2 = subject.login();

        // assert
        assertNotNull(session1);
        assertNotNull(session2);
        // second call to login provides the same session
        assertEquals(anonSession, session1);
        assertEquals(anonSession, session2);
    }

    @Test
    public void testLogin_initialized_userName() throws Throwable {

        // prepare
        final ActiveSession subject = new ActiveSession(repositoryRule, "user", "password");
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login(any(Credentials.class))).thenReturn(userSession);

        // act
        final Session session = subject.login();

        // assert
        assertNotNull(session);
        assertEquals(userSession, session);

        final ArgumentCaptor<SimpleCredentials> captor = ArgumentCaptor.forClass(SimpleCredentials.class);
        verify(repository).login(captor.capture());
        final SimpleCredentials passedParam = captor.getValue();
        assertEquals("user", passedParam.getUserID());
        assertEquals("password", String.valueOf(passedParam.getPassword()));
    }

    @Test(expected = AssertionError.class)
    public void testLogin_uninitialized() throws Exception {
        subject.login();
    }

    @Test
    public void testLoginStringString() throws Throwable {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login(any(Credentials.class))).thenReturn(userSession);

        // act
        final Session session = subject.login("user", "password");

        // assert
        assertNotNull(session);
        assertEquals(userSession, session);

        final ArgumentCaptor<SimpleCredentials> captor = ArgumentCaptor.forClass(SimpleCredentials.class);
        verify(repository).login(captor.capture());
        final SimpleCredentials passedParam = captor.getValue();
        assertEquals("user", passedParam.getUserID());
        assertEquals("password", String.valueOf(passedParam.getPassword()));
    }

    @Test(expected = AssertionError.class)
    public void testGetAdminSession_uninitialized() throws Throwable {
        subject.getAdminSession();
    }

    @Test
    public void testGetAdminSession_initialized() throws Throwable {
        // prepare
        BaseRuleHelper.setState(subject, BaseRule.State.INITIALIZED);
        when(repository.login(any(Credentials.class))).thenReturn(adminSession);

        // act
        final Session session = subject.getAdminSession();

        // assert
        assertNotNull(session);
        assertEquals(adminSession, session);

        final ArgumentCaptor<SimpleCredentials> captor = ArgumentCaptor.forClass(SimpleCredentials.class);
        verify(repository).login(captor.capture());
        final SimpleCredentials passedParam = captor.getValue();
        assertEquals("admin", passedParam.getUserID());
        assertEquals("admin", String.valueOf(passedParam.getPassword()));
    }

}

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

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.net.URL;
import java.security.Principal;

import io.tourniquet.junit.rules.BaseRule;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

import io.tourniquet.junit.rules.BaseRuleHelper;
import junit.framework.TestCase;

@RunWith(MockitoJUnitRunner.class)
public class JackrabbitContentRepositoryTest extends TestCase {

    private static final Logger LOG = getLogger(JackrabbitContentRepositoryTest.class);

    private final URL configUrl = getClass().getResource("JackrabbitContentRepositoryTest_repository.xml");

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private RepositoryImpl repositorySpy;

    private JackrabbitContentRepository subject;

    @Mock
    private Description description;

    @Before
    public void setUp() throws Exception {
        subject = new JackrabbitContentRepository(folder) {
            @Override
            public Repository getRepository() {
                repositorySpy = spy((RepositoryImpl) super.getRepository());
                return repositorySpy;
            }
        };
        subject.setConfigUrl(configUrl);
    }

    @After
    public void tearDown() throws Exception {

        subject.after();
    }


    @Test
    public void testAddUser() throws Throwable {
        //prepare
        final String username = "testuser";
        final String password = "password";

        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //act
                Principal user = subject.addUser(username, password);
                LOG.info("User {} created", user);

                //assert
                assertNotNull(user);
                assertEquals(username, user.getName());
                Session session = subject.getRepository().login(
                        new SimpleCredentials(username, password.toCharArray()));
                assertEquals(username, session.getUserID());

            }
        },description).evaluate();

        //assert

    }

    @Test(expected = AssertionError.class)
    public void testAddUser_userExists_fail() throws Throwable {
        //prepare
        final String username = "testuser";
        final String password = "password";

        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare
                subject.addUser(username, password);
                //act
                subject.addUser(username, password);

            }
        }, description).evaluate();

        //assert

    }

    @Test
    public void testDeleteUser_userExists_false() throws Throwable {
        //prepare
        final String username = "testuser";
        final String password = "password";


        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                final Principal user = subject.addUser(username, password);

                //act
                boolean result = subject.deleteUser(username);

                //assert
                assertNotNull(user);
                assertTrue(result);

            }
        },description).evaluate();
    }

    @Test
    public void testDeleteUser_userNotExists_false() throws Throwable {
        //prepare
        final String username = "testuser";
        final String password = "password";

        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                //act
                boolean result = subject.deleteUser(username);

                //assert
                assertFalse(result);

            }
        }, description).evaluate();
    }

    @Test
    public void testResetUsers() throws Throwable {
        //prepare
        final String user1 = "user1";
        final String user2 = "user2";

        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare
                subject.addUser(user1, user1);
                subject.addUser(user2, user2);

                //act
                subject.resetUsers();

                //assert
                UserManager um = ((JackrabbitSession) subject.getAdminSession()).getUserManager();
                assertNull(um.getAuthorizable(user1));
                assertNull(um.getAuthorizable(user2));

            }
        }, description).evaluate();

    }

    @Test
    public void testResetUsers_userDeleted_ok() throws Throwable {
        //prepare
        final String user1 = "user1";
        final String user2 = "user2";

        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare
                subject.addUser(user1, user1);
                subject.addUser(user2, user2);
                subject.deleteUser(user1);

                //act
                subject.resetUsers();

                //assert
                UserManager um = ((JackrabbitSession) subject.getAdminSession()).getUserManager();
                assertNull(um.getAuthorizable(user1));
                assertNull(um.getAuthorizable(user2));

            }
        }, description).evaluate();

    }

    @Test
    public void testResetUsers_userExternallyDeleted_ok() throws Throwable {
        //prepare
        final String user1 = "user1";
        final String user2 = "user2";

        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare
                subject.addUser(user1, user1);
                subject.addUser(user2, user2);
                final UserManager um = ((JackrabbitSession) subject.getAdminSession()).getUserManager();
                um.getAuthorizable(user1).remove();

                //act
                subject.resetUsers();

                //assert
                assertNull(um.getAuthorizable(user1));
                assertNull(um.getAuthorizable(user2));

            }
        }, description).evaluate();

    }

    @Test
    public void testResolvePrincipal_Everyone() throws Throwable {

        //act
        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare

                //act
                Principal principal = subject.resolvePrincipal("everyone");

                //assert
                assertNotNull(principal);
                assertEquals("everyone", principal.getName());

            }
        }, description).evaluate();

    }

    @Test
    public void testResolvePrincipal_Anonymous() throws Throwable {

        //act
        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare

                //act
                Principal principal = subject.resolvePrincipal("anonymous");

                //assert
                assertNotNull(principal);
                assertEquals("anonymous", principal.getName());

            }
        }, description).evaluate();

    }

    @Test
    public void testResolvePrincipal_User() throws Throwable {

        //act
        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare
                Principal expected = subject.addUser("user1", "password");

                //act
                Principal actual = subject.resolvePrincipal("user1");

                //assert
                assertNotNull(actual);
                assertEquals(expected, actual);
                assertEquals("user1", actual.getName());

            }
        }, description).evaluate();

    }

    @Test(expected = AssertionError.class)
    public void testResolvePrincipal_UnknownUser_fail() throws Throwable {

        //act
        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare

                //act
                subject.resolvePrincipal("user1");

            }
        }, description).evaluate();

    }

    @Test(expected = LoginException.class)
    public void testDeny() throws Throwable {
        // act
        subject.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                //prepare
                //act
                subject.deny("anonymous", "/", "jcr:read");

                //assert
                subject.getRepository().login();

            }
        }, description).evaluate();

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
}

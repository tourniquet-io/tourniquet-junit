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

import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.util.HashMap;
import java.util.Map;

import io.tourniquet.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link TestRule} for creating an active JCR session for a test.
 *
 */
public class ActiveSession extends ExternalResource<ContentRepository> {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActiveSession.class);

    private Session adminSession;
    private Session anonSession;
    private final Map<String, Session> userSessions = new HashMap<>();

    private final ContentRepository repositoryRule;
    private final String password;
    private final String username;

    public ActiveSession(final ContentRepository repository) {
        this(repository, null, null);
    }

    public ActiveSession(final ContentRepository repository, final String username, final String password) {
        super(repository);
        repositoryRule = repository;
        this.username = username;
        this.password = password;
    }

    /**
     * Closes all sessions
     */
    @Override
    protected void after() {
        super.after();
        if (adminSession != null) {
            LOG.info("Logging off {}", adminSession.getUserID());
            adminSession.logout();
            adminSession = null;
        }
        if (anonSession != null) {
            LOG.info("Logging off {}", anonSession.getUserID());
            anonSession.logout();
            anonSession = null;
        }
        for (final Session session : userSessions.values()) {
            LOG.info("Logging off {}", session.getUserID());
            session.logout();
        }
        userSessions.clear();
        LOG.info("Closed all sessions");
    }

    /**
     * @return the repository
     */
    public Repository getRepository() {
        assertStateAfterOrEqual(State.CREATED);
        return repositoryRule.getRepository();
    }

    /**
     * Logs into the repository. If a username and password has been specified, is is used for the login, otherwise an
     * anonymous login is done.
     *
     * @return the session for the login
     * @throws RepositoryException
     * @throws LoginException
     */
    public Session login() throws RepositoryException {
        assertStateAfterOrEqual(State.CREATED);
        final Session session;
        if (username != null && password != null) {
            session = getRepository().login(new SimpleCredentials(username, password.toCharArray()));
            userSessions.put(username, session);
        } else if (anonSession == null) {
            anonSession = getRepository().login();
            session = anonSession;
        } else {
            session = anonSession;
        }
        return session;
    }

    /**
     * Creates a login for the given username and password
     *
     * @param username
     *            the username to log in
     * @param password
     *            the password to log in
     * @return the session for the user
     * @throws RepositoryException
     */
    public Session login(final String username, final String password) throws RepositoryException {
        assertStateAfterOrEqual(State.CREATED);
        if (!userSessions.containsKey(username)) {
            userSessions.put(username, getRepository().login(new SimpleCredentials(username, password.toCharArray())));
        }
        return userSessions.get(username);
    }

    /**
     * @return a session with priviledges access rights
     * @throws RepositoryException
     */
    public Session getAdminSession() throws RepositoryException {
        assertStateAfterOrEqual(State.CREATED);
        if (adminSession == null) {
            adminSession = getRepository().login(new SimpleCredentials("admin", "admin".toCharArray()));
        }
        return adminSession;
    }
}

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

import static org.junit.Assert.assertNotNull;
import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.security.Privilege;
import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;

/**
 * The base implementation for Jackrabbit based repositories. Jackrabbit is the reference
 * implementation of the JCR standard.
 * <p>
 *     <b>Note:</b> if you're using this rule as a class rule, be sure to remove users that are added within a single
 *     test execution properly after the test.
 * </p>
 * Created by gerald on 03.06.15.
 */
public abstract class JackrabbitContentRepository extends ConfigurableContentRepository {

    private static final Logger LOG = getLogger(JackrabbitContentRepository.class);

    /**
     * The names of users that have been created using this rule. The set is used to track the user to delete them all
     * at once using the {@code resetUsers()} method.
     */
    private final transient Set<String> addedUsers;

    public JackrabbitContentRepository(final TemporaryFolder workingDirectory) {
        super(workingDirectory);
        this.addedUsers = new HashSet<>();
    }

    /**
     * Closes the admin session, and in case of local transient respository for unit test, shuts down the repository and
     * cleans all temporary files.
     */
    @Override
    protected void destroyRepository() {

        final RepositoryImpl repository = (RepositoryImpl) getRepository();
        repository.shutdown();
        LOG.info("Destroyed repository at {}", repository.getConfig().getHomeDir());
    }

    /**
     * Creates a transient repository with files in the local temp directory.
     *
     * @return the created repository
     *
     * @throws IOException
     *  if the repository configuration can not be read
     */
    @Override
    protected RepositoryImpl createRepository() throws IOException {

        try {
            final RepositoryConfig config = createRepositoryConfiguration();
            return RepositoryImpl.create(config);
        } catch (final ConfigurationException e) {
            LOG.error("Configuration invalid", e);
            throw new AssertionError(e.getMessage(), e);
        } catch (RepositoryException e) {
            LOG.error("Could not create repository", e);
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Override
    public Principal addUser(final String username, final String password) {

        try {
            final Session session = getAdminSession();
            final UserManager userManager = ((JackrabbitSession) session).getUserManager();
            final User user = userManager.createUser(username, password);
            this.addedUsers.add(username);
            return user.getPrincipal();
        } catch (RepositoryException e) {
            throw new AssertionError("Could not add user", e);
        }
    }

    @Override
    public boolean deleteUser(String username) {

        try {
            final Session session = getAdminSession();
            final UserManager userManager = ((JackrabbitSession) session).getUserManager();
            final User user = (User) userManager.getAuthorizable(username);
            if (user == null) {
                return false;
            }
            user.remove();
            this.addedUsers.remove(username);
            return true;
        } catch (RepositoryException e) {
            throw new AssertionError("Could not delete user",e);
        }
    }

    @Override
    public void resetUsers() {

        try {
            final Session session = getAdminSession();
            final UserManager userManager = ((JackrabbitSession) session).getUserManager();
            for (String userId : this.addedUsers) {
                final User user = (User) userManager.getAuthorizable(userId);
                if (user != null) {
                    user.remove();
                }
            }
            this.addedUsers.clear();
        } catch (RepositoryException e) {
            throw new AssertionError("Could not reset users", e);
        }

    }

    @Override
    protected Principal resolvePrincipal(final String principalId) throws RepositoryException {

        final Principal principal;

        final Session session = getAdminSession();
        if ("everyone".equals(principalId)) {
            principal = ((JackrabbitSession) session).getPrincipalManager().getEveryone();
        } else {
            final Authorizable authorizable = this.resolveAuthorizable(principalId);
            principal = authorizable.getPrincipal();
        }

        return principal;
    }

    /**
     * Denies the specified principal (user or group) on the specified resource one or more JCR permissions.
     *
     * @param principalId
     *         the id of the principal to deny privileges
     * @param path
     *         the path of the node to which a privilege should be applied
     * @param privilege
     *         the privileges to deny.
     */
    @Override
    public void deny(String principalId, String path, String... privilege) throws RepositoryException {

        final Session session = getAdminSession();

        final Privilege[] privilegeArray = toPrivilegeArray(session, privilege);
        final Principal principal = this.resolvePrincipal(principalId);

        AccessControlUtils.addAccessControlEntry(session, path, principal, privilegeArray, false);

        // and the session must be saved for the changes to be applied
        session.save();
    }

    /**
     * Resolves a name to an {@link Authorizable}. The name can be of a user or a group. The resulting authroziable can
     * be of type {@link org.apache.jackrabbit.api.security.user.User} or {@link org.apache.jackrabbit.api.security.user.Group}
     * and has to be checked and cast accordingly. The method will fail with an {@link AssertionError} if no such
     * authorizable exists.
     *
     * @param authorizableId
     *         the id of the authorizable
     *
     * @return the resolved {@link org.apache.jackrabbit.api.security.user.Authorizable}. It is either a {@link
     * org.apache.jackrabbit.api.security.user.Group} or a {@link org.apache.jackrabbit.api.security.user.User}.
     *
     * @throws RepositoryException
     *         if the name was not found
     */
    protected Authorizable resolveAuthorizable(final String authorizableId) throws RepositoryException {

        final Session session = getAdminSession();
        final UserManager userManager = ((JackrabbitSession) session).getUserManager();
        final Authorizable authorizable = userManager.getAuthorizable(authorizableId);
        assertNotNull("Could not resolve " + authorizableId, authorizable);
        return authorizable;
    }
}

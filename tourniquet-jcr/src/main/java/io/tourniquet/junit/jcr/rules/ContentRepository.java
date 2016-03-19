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

import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.security.AccessControlEntry;
import javax.jcr.security.AccessControlList;
import javax.jcr.security.AccessControlManager;
import javax.jcr.security.Privilege;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import io.tourniquet.junit.inject.InjectableHolder;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;

import io.tourniquet.junit.rules.ExternalResource;
import io.tourniquet.junit.security.SecurityTestHelper;

/**
 * Rule for testing with java content repositories (JCR). The rule implementations rely on the reference implementation
 * Jackrabbit. The Rule provides access to the {@link Repository} instance and logging in.
 *
 */
public abstract class ContentRepository extends ExternalResource<TemporaryFolder> implements
        InjectableHolder<Repository> {

    private static final Logger LOG = getLogger(ContentRepository.class);

    private static final String ANY_WILDCARD = "*";

    private final TemporaryFolder workingDirectory;
    /**
     * The JCR Repository.
     */
    private transient Repository repository;

    /**
     * Session with administrator privileges.
     */
    private transient Session adminSession;

    /**
     * Session for anonymous access
     */
    private Session anonSession;

    /**
     * Creates the content repository in the working directory.
     *
     * @param workingDirectory
     *  the working directory for temporary files.
     */
    public ContentRepository(final TemporaryFolder workingDirectory) {
        super(workingDirectory);
        this.workingDirectory = workingDirectory;
    }


    @Override
    protected void beforeClass() throws Throwable {

        this.doBefore();

    }


    @Override
    protected void afterClass() {

        this.doAfter();

    }

    @Override
    protected void before() throws Throwable {
        //call the before method if the repository is not yet created.
        // this check is required if the rule is used as class rule, where the repository
        // is initialized before the before method, to avoid initializing the repository twice
        if (isBeforeState(State.CREATED)) {
            this.doBefore();
            doStateTransition(State.BEFORE_EXECUTED);
        }
    }

    @Override
    protected void after() {
        //only tear down the repository in the after method, if the before method has been executed, indicating
        //this rule is used a an instance and no class rule. For class rules the doAfter() is executed on
        //tearing down the classrule using afterClass()
        if (isInState(State.BEFORE_EXECUTED)) {
            doAfter();
            doStateTransition(State.AFTER_EXECUTED);
        }
    }

    /**
     * Destroys the repository.
     */
    private void doAfter() { //NOSONAR

        if (this.isActive(this.adminSession)) {
            this.adminSession.logout();
        }
        this.destroyRepository();
        doStateTransition(State.DESTROYED);
    }

    /**
     * Checks if the specified session is not null and is still alive.
     * @param session
     *  the session to be checked
     * @return
     *  <code>true</code> if the session is still active
     */
    private boolean isActive(final Session session) {

        return session != null && session.isLive();
    }

    /**
     * Is invoked after the test has been executed. Implementation may perform actions to shutdown the repository
     * properly.
     */
    protected abstract void destroyRepository();

    /**
     * Creates and initializes the repository. At first the repository is created, transitioning the state to CREATED.
     * Afterwards the repository is initialized and transitioned to INITIALIZED.
     */
    private void doBefore() throws Throwable { //NOSONAR

        this.repository = this.createRepository();
        doStateTransition(State.CREATED);
        this.initialize();
        doStateTransition(State.INITIALIZED);
    }

    /**
     * Creates the JCR {@link Repository}. Implement this method to provide a repository instance suitable for
     * testing purposes.
     *
     * @return the created repository
     *
     * @throws Exception
     *         if the creation of the repository failed.
     */
    protected abstract Repository createRepository() throws Exception; // NOSONAR

    /**
     * Method that is invoked after creation to initialize the repository. Subclasses may override this
     * method to perform specific initialization logic.
     */
    protected void initialize() { //NOSONAR override is optional
    }

    /**
     * The repository wrapped by this rule.
     * @return the repository
     */
    public Repository getRepository() {
        assertStateAfterOrEqual(State.CREATED);
        return this.repository;
    }

    @Override
    public Repository getInjectionValue() {
        assertStateAfterOrEqual(State.CREATED);
        return this.repository;
    }

    /**
     * @return the {@link TemporaryFolder} referring to the workingDirectory in which the repository and its
     *         configuration is located. Some implementations may not need a working directory and therefore this value
     *         may be <code>null</code>
     */
    public TemporaryFolder getWorkingDirectory() {
        assertStateAfterOrEqual(State.CREATED);
        return this.workingDirectory;
    }

    /**
     * Logs into the repository with the given credentials. The created session is not managed and be logged out after
     * use by the caller.
     *
     * @param userId
     *            the user id to log in
     * @param password
     *            the password for the user
     * @return the {@link Session} for the user
     * @throws RepositoryException
     *  if the login failed for a repository internal error
     */
    public Session login(final String userId, final String password) throws RepositoryException {

        assertStateAfterOrEqual(State.CREATED);
        return this.repository.login(new SimpleCredentials(userId, password.toCharArray()));
    }

    /**
     * Adds a user with the given password to the repository. <p> <b>Note:</b> in case the rule is used as a class rule
     * you should ensure that you delete each created user properly. Otherwise consecutive calls will fail. You may also
     * invoke to cleanup the users created in one session using the {@code resetUsers()} method. </p> <p> The default
     * implementation throws an {@link UnsupportedOperationException} as it's up to the JCR implementation if and how
     * user management ist provided. </p>
     *
     * @param username
     *         the name of the user to add
     * @param password
     *         the password for the user
     *
     * @return the Principal representing the the newly created user.
     */
    public Principal addUser(final String username, final String password) {

        throw new UnsupportedOperationException("add user not supported");
    }

    /**
     * Removes a user from the repository. <p> The default implementation throws an {@link
     * UnsupportedOperationException} as it's up to the JCR implementation if and how user management ist provided.
     * </p>
     *
     * @param username
     *         the name of the user to remove
     *
     * @return <code>true</code> if the user was found and successfully deleted. <code>false</code> if no such user
     * existed
     */
    public boolean deleteUser(String username) {

        throw new UnsupportedOperationException("delete user not supported");
    }

    /**
     * Removes all users from the repository that have been created using this rule. If users are already removed the
     * method will not fail. <p> The default implementation throws an {@link UnsupportedOperationException} as it's up
     * to the JCR implementation if and how user management ist provided. </p>
     */
    public void resetUsers() {

        throw new UnsupportedOperationException("reset user not supported");
    }

    /**
     * Grants the specified principal (user or group) on the specified resource one or more JCR permissions.
     *
     * @param principalId
     *         the id of the principal to grant privileges
     * @param path
     *         the path of the node to which a privilege should be applied
     * @param privileges
     *         the privileges to grant.
     */
    public void grant(String principalId, String path, String... privileges) throws RepositoryException {

        final Session session = this.getAdminSession();
        final AccessControlManager acm = session.getAccessControlManager();

        final Privilege[] privilegeArray = this.toPrivilegeArray(session, privileges);
        final AccessControlList acl = this.getAccessControlList(session, path);
        final Principal principal = this.resolvePrincipal(principalId);
        // add a new one for the special "everyone" principal
        acl.addAccessControlEntry(principal, privilegeArray);

        // the policy must be re-set
        acm.setPolicy(path, acl);

        // and the session must be saved for the changes to be applied
        session.save();

    }

    /**
     * Logs into the repository as admin user. The session should be logged out after each test if the repository is
     * used as a {@link org.junit.ClassRule}.
     *
     * @return a session with admin privileges
     *
     * @throws RepositoryException
     *         if the login failed for any reason
     */
    public Session getAdminSession() throws RepositoryException {

        if(this.isActive(this.adminSession)){
            //perform a refresh to update the session to the latest repository version
            this.adminSession.refresh(false);
        } else {
            this.adminSession = this.repository.login(new SimpleCredentials("admin", "admin".toCharArray()));
        }

        return this.adminSession;
    }

    /**
     * Converts the list of privilege names to an array of {@link Privilege}s.
     *
     * @param session
     *         the current session that provides the {@link AccessControlManager}
     * @param privileges
     *         the privileges to convert
     *
     * @return an array of {@link Privilege}s
     *
     * @throws RepositoryException
     *         if the access control manager could not be retrieved of the list of privileges contained unsupported
     *         privileges
     */
    protected Privilege[] toPrivilegeArray(Session session, final String... privileges) throws RepositoryException {

        final AccessControlManager acm = session.getAccessControlManager();

        final List<Privilege> privilegeList = new ArrayList<>();
        for (String p : privileges) {
            privilegeList.add(acm.privilegeFromName(p));
        }
        return privilegeList.toArray(new Privilege[privilegeList.size()]);
    }

    /**
     * Retrieves the {@link AccessControlList} for a given path. If there is no ACL present, a new one will be created.
     *
     * @param session
     *         the current session that provides the {@link AccessControlManager}
     * @param path
     *         the path for which the ACL should be retrieved.
     *
     * @return the access control list for the path
     *
     * @throws RepositoryException
     *  when the access control manager could not be retrieved or the ACLs of the specified path could not be
     *  obtained.
     */
    protected AccessControlList getAccessControlList(Session session, final String path) throws RepositoryException {

        final AccessControlManager acm = session.getAccessControlManager();
        AccessControlList acl;
        try {
            // get first applicable policy (for nodes w/o a policy)
            acl = (AccessControlList) acm.getApplicablePolicies(path).nextAccessControlPolicy();
        } catch (NoSuchElementException e) {
            LOG.debug("no applicable policy found", e);

            // else node already has a policy, get that one
            acl = (AccessControlList) acm.getPolicies(path)[0];
        }
        return acl;
    }

    /**
     * Method to resolve a user ID to a {@link Principal}. The default implementation will provide a custom principal
     * implementation that may well suit the needs. If the JCR implementation requires an implementation specific
     * Principal, it is recommended to override this method with an JCR implementation specific implementation.
     *
     * @param principalId
     *         the principal Id to be resolved
     *
     * @return the {@link Principal} that reflects the ID.
     */
    protected Principal resolvePrincipal(final String principalId) throws RepositoryException {

        return SecurityTestHelper.toPrincipal(principalId);
    }

    /**
     * Logs into the repository as anonymous user. The session should be logged out after each test if the repository is
     * used as a {@link org.junit.ClassRule}.
     * @return
     *  a session for the anonymous principal. In most default configurations the anonymous user has only read
     *  permissions.
     * @throws RepositoryException
     *  if the login failed for any reason
     */
    public Session login() throws RepositoryException {

        if (this.isActive(this.anonSession)) {
            //perform a refresh to update the session to the latest repository version
            this.anonSession.refresh(false);
        } else {
            this.anonSession = this.repository.login();
        }

        return this.anonSession;
    }

    /**
     * Denys a specific privilege to a user on a node specified by the path. Different to the {@code grant()} method the
     * deny capability is not covered by the JCR specification and is therefore dependant on the the vendor specific
     * implementation. Therefore the default implementation will throw an {@link UnsupportedOperationException}.
     *
     * @param principalId
     *         the id of the principal to whom a specific privilege should be denied
     * @param path
     *         the path of the node on which the privilege should be denied
     * @param privilege
     *         one or more privileges to be denied
     *
     * @throws RepositoryException
     *  if the privilege can not be denied
     */
    public void deny(String principalId, String path, String... privilege) throws RepositoryException {

        throw new UnsupportedOperationException("deny is not supported by this implementation");
    }

    /**
     * Removes all ACLs on the node specified by the path.
     *
     * @param path
     *         the absolute path to the node
     * @param principalNames
     *         the user(s) whose ACL entries should be removed. If none is provided, all ACLs will be removed. A word
     *         of warning, if you invoke this on the root node '/' all ACls including those for administrators
     *         and everyone-principal will be removed, rendering the entire repository useless.
     */
    public void clearACL(String path, String... principalNames) throws RepositoryException {

        final Session session = this.getAdminSession();
        final AccessControlManager acm = session.getAccessControlManager();
        final AccessControlList acl = this.getAccessControlList(session, path);

        final String[] principals;
        if (principalNames.length == 0) {
            // remove all existing entries
            principals = new String[] { ANY_WILDCARD };
        } else {
            principals = principalNames;
        }

        for (String username : principals) {
            this.removeAccessControlEntries(acl, username);
        }

        // the policy must be re-set
        acm.setPolicy(path, acl);

        session.save();
    }

    /**
     * Removes all entries from the {@link AccessControlList} that match the given principal name.
     *
     * @param acl
     *         the acl from which the entries should be removed
     * @param principalName
     *         the name of the principal whose matching entries should be removed. If the wildcard '*' is used, all
     *         entries will be removed.
     *
     * @throws RepositoryException
     *         if the entries could not be removed
     */
    private void removeAccessControlEntries(final AccessControlList acl, final String principalName)
            throws RepositoryException {

        for (final AccessControlEntry e : acl.getAccessControlEntries()) {
            this.removeAccessControlEntry(acl, e, principalName);
        }
    }

    /**
     * Removes the specified access control entry if the given principal name matches the principal associated with the
     * entry.
     *
     * @param acList
     *         the access control list to remove the entry from
     * @param acEntry
     *         the entry to be potentially removed
     * @param principalName
     *         the name of the principal to match. Use the '*' wildcard to match all principal.
     *
     * @throws RepositoryException
     *         if the entry removal failed
     */
    private void removeAccessControlEntry(final AccessControlList acList,
                                          final AccessControlEntry acEntry,
                                          final String principalName) throws RepositoryException {

        if (ANY_WILDCARD.equals(principalName) || acEntry.getPrincipal().getName().equals(principalName)) {
            acList.removeAccessControlEntry(acEntry);
        }
    }


}

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

package io.tourniquet.junit.security;

import javax.security.auth.Subject;
import java.lang.reflect.Method;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public final class SecurityTestHelper {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(SecurityTestHelper.class);

    private SecurityTestHelper() {
    }

    /**
     * Invokes the specified method on the target in the JAAS Context of the given user
     *
     * @param user
     *            the user in whose context the method should be invoked
     * @param target
     *            the target object of the method invocation or <code>null</code> if it is a static method
     * @param method
     *            the method to be invoked
     * @param params
     *            the parameters passed to the method
     * @return the result of the method invocation
     * @throws Throwable
     *             if the invokation failed for any reason
     */
    public static <T> T invokeAs(final String user, final Object target, final Method method, final Object... params)
            throws Throwable { // NOSONAR
        return invokeAs(subjectForUser(user), target, method, params);
    }

    /**
     * Invokes the specified method on the target in the JAAS Context of the given subject
     *
     * @param jaasSubject
     *            the subject in whose context the method should be invoked
     * @param target
     *            the target object of the method invocation or <code>null</code> if it is a static method
     * @param method
     *            the method to be invoked
     * @param params
     *            the parameters passed to the method
     * @return the result of the method invocation
     * @throws Throwable
     *             if the invocation failed for any reason
     */
    public static <T> T invokeAs(final Subject jaasSubject, final Object target, final Method method,
            final Object... params) throws Throwable { // NOSONAR

        try {
            return Subject.doAs(jaasSubject, new PrivilegedExceptionAction<T>() {

                @SuppressWarnings("unchecked")
                @Override
                public T run() throws Exception {
                    return (T) method.invoke(target, params);
                }
            });
        } catch (final PrivilegedActionException e) {
            LOG.debug("Exception in privileged action", e);
            throw e.getCause();
        }

    }

    /**
     * Creates a subject for a user with the given name
     *
     * @param userName
     *            name of the user
     * @return a subject for the user
     */
    public static Subject subjectForUser(final String userName) {

        final SimpleUserPrincipal principal = new SimpleUserPrincipal(userName);
        return subjectForUser(principal);

    }

    /**
     * Creates a subject for the given user principal
     *
     * @param userPrincipal
     *         the user principal to be added to the subject
     *
     * @return the subject for the user
     */
    public static Subject subjectForUser(final Principal userPrincipal) {

        final Set<Principal> principals = new HashSet<>();
        principals.add(userPrincipal);

        return new Subject(false, principals, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Creates a {@link Principal} for the given user name. The principal will be of type {@link SimpleUserPrincipal} so
     * it might not be useful, if a certain {@link Principal} implementation is required.
     *
     * @param userName
     *         the user name for which a {@link Principal} should be created.
     *
     * @return a principal reflecting the user name
     */
    public static Principal toPrincipal(final String userName) {

        return new SimpleUserPrincipal(userName);
    }
}

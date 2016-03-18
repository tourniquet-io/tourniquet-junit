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

package io.tourniquet.junit.jcr.rules.builder;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

import io.tourniquet.junit.jcr.rules.JNDIContentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link io.tourniquet.junit.Builder} for creating a {@link io.tourniquet.junit.jcr.rules.JNDIContentRepository}. The builder allows to override the default lookup
 * name as well setting a {@link Context} or configuring the context to create. If no context property is specified the
 * default {@link Context} will be used.
 *
 */
public class JNDIContentRepositoryBuilder extends ContentRepositoryBuilder<JNDIContentRepository> {

    /**
     * SLF4J Logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(JNDIContentRepositoryBuilder.class);

    /**
     * The content repository rule to build
     */
    private final JNDIContentRepository contentRepository;

    /**
     * Properties to use for creating the context
     */
    private Properties contextProperties = new Properties();
    /**
     * A Naming context that is used if specified.
     */
    private Context context;

    public JNDIContentRepositoryBuilder() {
        contentRepository = new JNDIContentRepository();
    }

    @Override
    public JNDIContentRepository build() {
        final Context ctx;
        if (context != null) {
            ctx = context;
        } else {
            try {
                if (contextProperties.isEmpty()) {
                    // create default initial context
                    ctx = new InitialContext();
                } else {
                    ctx = new InitialContext(contextProperties);
                }
            } catch (final NamingException e) {
                LOG.error("Repository Lookup failed", e);
                throw new AssertionError("Rule building failed: " + e.getMessage(), e);
            }
        }
        contentRepository.setContext(ctx);
        return contentRepository;
    }

    /**
     * Sets the context to be used for the JNDI lookup. If no context is set or <code>null</code> is passed, as new
     * initial context will be created before the lookup.
     *
     * @param context
     *            the context to use
     * @return this builder
     */
    public JNDIContentRepositoryBuilder usingContext(final Context context) {
        this.context = context;
        return this;
    }

    /**
     * Set the lookup name to use for the {@link Repository} lookup. This will override the default value.
     *
     * @param lookupName
     *            the new lookup name to use
     * @return this test rule
     * @see JNDIContentRepository
     */
    public JNDIContentRepositoryBuilder withLookup(final String lookupName) {
        contentRepository.setLookupName(lookupName);
        return this;
    }

    /**
     * Uses the given properties as environment for the lookup context. Replaces the current context environment.
     *
     * @param properties
     *            the properties to use for the environment for the context initialization
     * @return this test rule
     */
    public JNDIContentRepositoryBuilder withContextProperties(final Properties properties) {

        contextProperties = (Properties) properties.clone();
        return this;
    }

    /**
     * Adds a new context property to the environment for the JNDI lookup context
     *
     * @param name
     *            the name of the environment variable
     * @param value
     *            the value to assign to the variable
     * @return this test rule
     */
    public JNDIContentRepositoryBuilder withContextProperty(final String name, final Object value) {

        contextProperties.put(name, value);
        return this;
    }

    /**
     * Uses the specified contextFactory as initial context factory. The method is a convenience for setting the
     * INITIAL_CONTEXT_FACTORY property on the environment.
     *
     * @param contextFactory
     *            the context factory class to use to create the context
     * @return this test rule
     */
    public JNDIContentRepositoryBuilder withInitialContextFactory(final Class<?> contextFactory) {

        return withInitialContextFactory(contextFactory.getName());
    }

    /**
     * Uses the specified contextFactory as initial context factory. The method is a convenience for setting the
     * INITIAL_CONTEXT_FACTORY property on the environment.
     *
     * @param contextFactory
     *            the context factory class name to use to create the context
     * @return this test rule
     */
    public JNDIContentRepositoryBuilder withInitialContextFactory(final String contextFactory) {

        contextProperties.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        return this;
    }

    /**
     * Uses the specified naming service provider. The method is a convenience for setting the PROVIDER_URL property on
     * the environment.
     *
     * @param providerUrl
     *            the provider URL to use for naming services. For JBoss the URL is like
     *            <code>remote://localhost:4447</code>. In case JBoss Remote naming should be used, the context property
     *            <code>jboss.naming.client.ejb.context</code> has to be set to <code>true</code>
     * @return this test rule
     */
    public JNDIContentRepositoryBuilder withProviderURL(final String providerUrl) {

        contextProperties.put(Context.PROVIDER_URL, providerUrl);
        return this;
    }

    /**
     * Sets the context properties for SECURITY_PRINCIPAL and SECURITY_CREDENTIAL to perform the lookup. This method is
     * a convenience for setting the properties SECURITY_PRINCIPAL and SECURITY_CREDENTIAL on the environment.
     *
     * @param principalName
     *            the principal name to use to perform the lookup
     * @param credentials
     *            the credentials used to authenticate the principal
     * @return this test rule
     */
    public JNDIContentRepositoryBuilder withSecurityPrincipal(final String principalName, final String credentials) {

        contextProperties.put(Context.SECURITY_PRINCIPAL, principalName);
        contextProperties.put(Context.SECURITY_CREDENTIALS, credentials);
        return this;
    }

}

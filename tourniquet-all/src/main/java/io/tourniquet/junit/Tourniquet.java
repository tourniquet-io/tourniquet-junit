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

package io.tourniquet.junit;

import io.tourniquet.junit.builder.TemporaryFolderBuilder;
import io.tourniquet.junit.inject.Injection;
import io.tourniquet.junit.jcr.rules.JNDIContentRepository;
import io.tourniquet.junit.jcr.rules.MockContentRepository;
import io.tourniquet.junit.jcr.rules.builder.InMemoryContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.JNDIContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.MockContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.StandaloneContentRepositoryBuilder;
import io.tourniquet.junit.rules.ldap.builder.DirectoryBuilder;
import io.tourniquet.junit.rules.ldap.builder.DirectoryServerBuilder;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * Main utility class for the Tourniquet JUnit test framework.
 * <h2>Injection support</h2>
 * Tourniquet allows injecting
 * object into test subjects or any other objects without having to use a full-blown CDI container. This may be useful
 * and more lightweight for simple Unit Testing. If the test has more of an integration test character, a CDI container
 * should be used instead. <h2>Test Rules</h2> Tourniquet provides a set of technology specific test rules:
 * <ul>
 * <li>Java Content Repository (JCR, JSR 170, JSR283)</li>
 * <li>Lightweight Directory Access Protocol (LDAP)</li>
 * <li>Folders and Files</li>
 * </ul>
 * <h3>Test Rules and Rule Chain, JUnit way</h3> To create a test rule, JUnit test rules may be instantiated directly:
 *
 * <pre>
 * {@literal @}Rule
 * TemporaryFolder folder = new TemporaryFolder();
 * </pre>
 * <p>
 * If you need a chain of rules, that are nested, JUnit provides the {@link RuleChain}:
 * </p>
 * <pre>
 * {@literal @}Rule
 * TestRule chain = RuleChain.outerRouler(new OuterRule()).around(new InnerRule());
 * </pre>
 * <p>
 * This approach is perfectly alright but is a bit awkward when you have rules in the chain, that depend on each other.
 * In that case you have to instantiate the rules before creating the chain:
 * </p>
 * <pre>
 * OuterRule outer = new OuterRule();
 * InnerRule inner = new InnerRule(outer); //inner depends on outer
 * {@literal @}Rule
 * TestRule chain = RuleChain.outerRouler(outer).around(inner);
 * </pre>
 *
 * <h3>Test Rule Chains, Tourniquet way</h3>
 * <p>
 * Tourniquet provides a set of rules that have dependencies on other rules. But
 * the rules may be created using a builder pattern that allows the creation and configuration of interdependent rule in
 * single line statements, that make the test setup more readable. The dependency mechanism is provided in the
 * {@link io.tourniquet.junit.rules.BaseRule} on which all the Tourniquet rules depend on. Furhter, the {@link Tourniquet} utility class provides
 * factory methods for creating specific rule builders.
 * </p>
 * <br>
 * Examples:
 * <ul>
 * <li>Creating a temporary file with content from a classpath resource in a temporary folder:<br>
 * Rule for a file with content
 *
 * <pre>
 * {@literal @}Rule
 * TemporaryFile file = Tourniquet.newTempFolder().aroundTempFile("example.txt").withContent().fromClasspathResource("/test/example.txt").build();
 * </pre>
 *
 * Rule for an empty file
 *
 * <pre>
 * {@literal @}Rule
 * TemporaryFile file = Tourniquet.newTempFolder().aroundTempFile("example.txt").build();
 * </pre>
 *
 * </li>
 * <li>Creating an in memory JCR content repository
 *
 * <pre>
 * {@literal @}Rule
 * InMemoryContentRepository rules = Tourniquet.newTempFolder().aroundInMemoryContentRepository().build();
 * </pre>
 *
 * </li>
 * <li>Creating an active JCR Session
 *
 * <pre>
 * {@literal @}Rule
 * ActiveSession jcrSession = Tourniquet.newTempFolder().aroundInMemoryContentRepository().aroundSession().build();
 * </pre>
 *
 * </li>
 * <li>Creating an content repository with initialized content.
 *
 * <pre>
 * {@literal @}Rule
 * ContentLoader content = Tourniquet.newTempFolder().aroundInMemoryContentRepository().aroundPreparedContent().fromUrl(someUrl).build();
 * </pre>
 *
 * </li>
 * </ul>
 *
 */
public final class Tourniquet {

    private Tourniquet() {
    }

    /**
     * Helper method to create an {@link Injection} support instance for the value.
     *
     * @param value
     *            the value to be injected
     * @return an {@link Injection} support for injecting the value into a target
     */
    public static Injection inject(final Object value) {
        return new Injection(value);
    }

   /**
     * Creates a new {@link Builder} for a {@link MockContentRepository}.
     *
     * @return a {@link MockContentRepositoryBuilder}
     */
    public static MockContentRepositoryBuilder newMockContentRepository() {

        return new MockContentRepositoryBuilder();
    }

    /**
     * Creates a new {@link Builder} for a {@link JNDIContentRepository}.
     *
     * @return a {@link JNDIContentRepositoryBuilder}
     */
    public static JNDIContentRepositoryBuilder newJndiContentRepository() {

        return new JNDIContentRepositoryBuilder();
    }

    /**
     * Convenient method for {@code newTempFolder().aroundInMemoryContentRepository()}.
     *
     * @return a builder for an in-memory content repository
     */
    public static InMemoryContentRepositoryBuilder newInMemoryContentRepository() {

        return newTempFolder().aroundInMemoryContentRepository();
    }

    /**
     * Creates a new {@link Builder} for a {@link TemporaryFolder}. The {@link TemporaryFolderBuilder} allows further
     * chaining of rules that require a temporary folder. <br>
     * To create a new Temporary folder there are two options:
     * <ul>
     * <li>instantiate it directly, using the JUnit way:
     *
     * <pre>
     * TemporaryFolder folder = new TemporaryFolder();
     * </pre>
     *
     * </li>
     * <li>instantiate it with a builder (the Tourniquet way):
     *
     * <pre>
     * TemporaryFolder folder = Tourniquet.newTempFolder().build();
     * </pre>
     *
     * This is slightly longer but allows chaining of dependent rules.</li>
     * </ul>
     *
     * @return a {@link TemporaryFolderBuilder}.
     */
    public static TemporaryFolderBuilder newTempFolder() {
        return new TemporaryFolderBuilder();
    }

    /**
     * Convenient method for
     * {@code newTempFolder().aroundStandaloneContentRepository()}.
     * @return
     *  a builder for a standalone content repository
     */
    public static StandaloneContentRepositoryBuilder newStandaloneContentRepository() {

        return newTempFolder().aroundStandaloneContentRepository();
    }

    /**
     * Convenient method for {@code newTempFolder().aroundDirectory()}.
     *
     * @return a builder for an ldap service
     */
    public static DirectoryBuilder newDirectory() {

        return newTempFolder().aroundDirectory();
    }


    /**
     * Convenient method for
     * {@code newTempFolder().aroundDirectory().aroundDirectoryServer()}.
     * @return
     *  a builder for a ldap directory server
     */
    public static DirectoryServerBuilder newDirectoryServer() {

        return newTempFolder().aroundDirectory().aroundDirectoryServer();
    }
}

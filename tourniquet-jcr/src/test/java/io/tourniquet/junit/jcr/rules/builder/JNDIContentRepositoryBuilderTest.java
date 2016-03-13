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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.Repository;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import io.tourniquet.junit.jcr.rules.JNDIContentRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 */
@RunWith(MockitoJUnitRunner.class)
public class JNDIContentRepositoryBuilderTest {

    @Mock
    private Statement statement;

    @Mock
    private Description description;

    @Mock
    private Context context;

    @Mock
    private Repository repository;

    public static class TestInitialContextFactory implements InitialContextFactory {

        static AtomicReference<Context> context = new AtomicReference<>();
        static AtomicReference<Hashtable<?, ?>> capturedEnvironment = new AtomicReference<>();

        @Override
        public Context getInitialContext(final Hashtable<?, ?> environment) throws NamingException {

            capturedEnvironment.set(environment);

            return context.get();
        }
    }

    /**
     * The class under test
     */
    private JNDIContentRepositoryBuilder subject;

    @Before
    public void setUp() throws Exception {

        TestInitialContextFactory.context.set(context);
        when(context.lookup("java:/rules/local")).thenReturn(repository);
        subject = new JNDIContentRepositoryBuilder();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBuild() throws Exception {

        //prepare

        //act
        final JNDIContentRepository repository = subject.build();

        //assert
        assertNotNull(repository);
    }

    @Test
    public void testUsingContext() throws Throwable {

        //prepare

        //act
        final JNDIContentRepositoryBuilder builder = subject.usingContext(context);

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());

    }

    @Test
    public void testWithLookup() throws Throwable {
        //prepare
        when(context.lookup("myLookup")).thenReturn(repository);
        subject.usingContext(context);

        //act
        final JNDIContentRepositoryBuilder builder = subject.withLookup("myLookup");

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());
    }

    @Test
    public void testWithContextProperties() throws Throwable {

        //prepare
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial",
                       "io.tourniquet.junit.jcr.rules.builder"
                               + ".JNDIContentRepositoryBuilderTest$TestInitialContextFactory");

        //act
        final JNDIContentRepositoryBuilder builder = subject.withContextProperties(properties);
        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());
    }

    @Test
    public void testWithContextProperty() throws Throwable {
        //prepare

        //act
        final JNDIContentRepositoryBuilder builder = subject.withContextProperty("java.naming.factory.initial",
                                                                                 "io.tourniquet.junit.jcr.rules"
                                                                                         + ".builder.JNDIContentRepositoryBuilderTest$TestInitialContextFactory");

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());
    }

    @Test
    public void testWithInitialContextFactory() throws Throwable {

        //act
        final JNDIContentRepositoryBuilder builder = subject.withInitialContextFactory(
                "io.tourniquet.junit.jcr.rules.builder.JNDIContentRepositoryBuilderTest$TestInitialContextFactory");

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());
    }

    @Test
    public void testWithInitialContextFactoryClass() throws Throwable {
        //act
        final JNDIContentRepositoryBuilder builder = subject.withInitialContextFactory(TestInitialContextFactory.class);

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());
    }

    @Test
    public void testWithProviderURL() throws Throwable {

        //prepare
        subject.withInitialContextFactory(TestInitialContextFactory.class);

        //act
        final JNDIContentRepositoryBuilder builder = subject.withProviderURL("someProvider");

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());

        assertEquals("someProvider",
                     TestInitialContextFactory.capturedEnvironment.get().get("java.naming.provider.url"));

    }

    @Test
    public void testWithSecurityPrincipal() throws Throwable {

        //prepare
        subject.withInitialContextFactory(TestInitialContextFactory.class);

        //act
        final JNDIContentRepositoryBuilder builder = subject.withSecurityPrincipal("principalName", "principalPw");

        //assert
        assertSame(subject, builder);
        final JNDIContentRepository rule = buildApplyEvaluate(builder);
        assertEquals(repository, rule.getRepository());

        assertEquals("principalName",
                     TestInitialContextFactory.capturedEnvironment.get().get("java.naming.security.principal"));
        assertEquals("principalPw",
                     TestInitialContextFactory.capturedEnvironment.get().get("java.naming.security.credentials"));
    }

    private JNDIContentRepository buildApplyEvaluate(final JNDIContentRepositoryBuilder builder) throws Throwable {

        final JNDIContentRepository rule = builder.build();
        assertNotNull(rule);
        rule.apply(statement, description).evaluate();
        verify(statement).evaluate();
        return rule;
    }
}

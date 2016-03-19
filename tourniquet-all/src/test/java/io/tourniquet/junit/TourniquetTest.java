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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jcr.Session;
import java.net.URL;

import io.tourniquet.junit.builder.TemporaryFolderBuilder;
import io.tourniquet.junit.inject.Injection;
import io.tourniquet.junit.jcr.rules.InMemoryContentRepository;
import io.tourniquet.junit.jcr.rules.StandaloneContentRepository;
import io.tourniquet.junit.jcr.rules.builder.InMemoryContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.JNDIContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.MockContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.StandaloneContentRepositoryBuilder;
import io.tourniquet.junit.rules.ldap.Directory;
import io.tourniquet.junit.rules.ldap.DirectoryServer;
import io.tourniquet.junit.jcr.JCRAssert;
import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TourniquetTest {

    @Mock
    private Description description;

    @Test
    public void testInject() throws Exception {

        //prepare
        String value = "123";

        //act
        Injection result = Tourniquet.inject(value);

        //assert
        assertNotNull(result);
    }

    @Test
    public void testInjectIntoAll() throws Exception {
        //prepare
        String var = "123";
        SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        Tourniquet.inject(var).intoAll(target);

        //assert
        assertEquals(var, target.injectionTarget1);
        assertEquals(var, target.injectionTarget2);
        assertEquals(var, target.injectionTarget3);

    }

    @Test
    public void testInjectInto() throws Exception {
        //prepare
        String var = "123";
        SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        Tourniquet.inject(var).into(target);

        //assert
        assertEquals(var, target.injectionTarget1);
        assertNull(var, target.injectionTarget2);
        assertNull(var, target.injectionTarget3);

    }

    @Test
    public void testInjectInto_ValidValue_valueInjected() throws Exception {
        //prepare
        // create an injection with no value (null) for a config property with a default value
        final SimpleInjectionTarget target = new SimpleInjectionTarget();

        // act
        Tourniquet.inject("testString").asConfigProperty("config.property.default").into(target);

        // assert
        assertEquals("testString", target.configPropertyWithDefault);
    }

    @Test
    public void testInjectAsConfigPropertyInto() throws Exception {
        //prepare
        String var = "123";
        SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        Tourniquet.inject(var).asConfigProperty("property1").into(target);

        //assert
        assertEquals(var, target.injectionTarget2);
        assertNull(target.injectionTarget1);
        assertNull(target.injectionTarget3);
    }

    @Test
    public void testInjectAsResourceInto() throws Exception {
        //prepare
        String var = "123";
        SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        Tourniquet.inject(var).asResource().byLookup("java:/any/name").into(target);

        //assert
        assertEquals(var, target.injectionTarget3);
        assertNull(target.injectionTarget1);
        assertNull(target.injectionTarget2);
    }

    @Test
    public void testInjectPrimitiveAsConfigPropertyInto() throws Exception {
        //prepare
        int var = 123;
        SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        Tourniquet.inject(var).asConfigProperty("property2").into(target);

        //assert
        assertEquals(var, target.primitiveInt);
    }

    @Test
    public void testInjectInto_NullValue_defaultInjected() throws Exception {
        //prepare
        final SimpleInjectionTarget target = new SimpleInjectionTarget();

        // act
        Tourniquet.inject(null).asConfigProperty("config.property.default").into(target);

        // assert
        assertEquals("defaultValue", target.configPropertyWithDefault);
    }

    @Test
    public void testInjectInto_nonConvertibleType_valueInjected() throws Exception {
        //prepare
        final SimpleInjectionTarget target = new SimpleInjectionTarget();
        Object object = new Object();

        // act
        Tourniquet.inject(object).asConfigProperty("config.property").into(target);

        // assert
        assertEquals(object, target.nonMatchingNonAutoConvertible);
    }

    @Test
    public void testNewTempFolder() throws Exception {

        //act
        TemporaryFolderBuilder result = Tourniquet.newTempFolder();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testNewMockContentRepository() throws Exception {

        //act
        MockContentRepositoryBuilder result = Tourniquet.newMockContentRepository();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testNewJNDIContextRepository() throws Exception {

        //act
        JNDIContentRepositoryBuilder result = Tourniquet.newJndiContentRepository();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testNewInMemoryContentRepository() throws Exception {

        //act
        InMemoryContentRepositoryBuilder result = Tourniquet.newInMemoryContentRepository();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testNewInMemoryContentRepository_withCnd() throws Throwable {

        //prepare
        final URL cndModel = getClass().getResource("TourniquetTest_testModel.cnd");

        //act
        //this is the actual line how the rule builder should be used in a test
        final InMemoryContentRepository result = Tourniquet.newInMemoryContentRepository()
                                                           .withNodeTypes(cndModel)
                                                           .build();

        //assert
        assertNotNull(result);
        result.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                final Session session = result.login("admin", "admin");
                JCRAssert.assertNodeTypeExists(session, "test:testType");
            }
        }, description).evaluate();
    }

    @Test
    public void testNewStandaloneContentRepository() throws Exception {
        //act
        StandaloneContentRepositoryBuilder result = Tourniquet.newStandaloneContentRepository();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testNewStandaloneContentRepository_withCnd() throws Throwable {

        //prepare
        final URL cndModel = getClass().getResource("TourniquetTest_testModel.cnd");

        //act
        //this is the actual line how the rule builder should be used in a test
        final StandaloneContentRepository result = Tourniquet.newStandaloneContentRepository()
                                                             .withNodeTypes(cndModel)
                                                             .build();

        //assert
        assertNotNull(result);

        result.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                final Session session = result.login("admin", "admin");
                JCRAssert.assertNodeTypeExists(session, "test:testType");
            }
        }, description).evaluate();
    }

    @Test
    public void testNewDirectory() throws Exception {
        //prepare

        //act
        final Directory dir = Tourniquet.newDirectory().build();

        //assert
        assertNotNull(dir);

    }

    @Test
    public void testNewDirectoryServer() throws Exception {
        //prepare

        //act
        final DirectoryServer ds = Tourniquet.newDirectoryServer().build();

        //assert
        assertNotNull(ds);
    }

    /**
     * Test to verify the rule is properly applicable and initialized with users from an ldif.
     *
     * @throws Throwable
     */
    @Test
    public void testNewDirectoryServerWithContent() throws Throwable {
        //prepare

        //act
        final URL ldif = getClass().getResource("TourniquetTest_testUsers.ldif");
        final DirectoryServer ds = Tourniquet.newDirectory()
                                             .withPartition("tourniquet", "dc=tourniquet")
                                             .importLdif(ldif)
                                             .aroundDirectoryServer()
                                             .onAvailablePort()
                                             .build();
        //assert
        assertNotNull(ds);
        ds.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertTrue(ds.getDirectoryService().getSession().exists("uid=testuser,ou=users,dc=tourniquet"));
            }
        }, description).evaluate();
    }

    static class SimpleInjectionTarget {

        String injectionTarget1;

        @Inject
        @ConfigProperty(name = "property1")
        String injectionTarget2;

        @Resource(lookup = "java:/any/name")
        String injectionTarget3;

        @Inject
        @ConfigProperty(name = "property2")
        int primitiveInt;

        @ConfigProperty(name = "config.property")
        String noInjectConfigProperty;

        @Inject
        @ConfigProperty(name = "config.property")
        String configProperty;

        @Inject
        @ConfigProperty(name = "config.property.default",
                        defaultValue = "defaultValue")
        String configPropertyWithDefault;

        @Inject
        String noConfigProperty;

        @Inject
        @ConfigProperty(name = "config.property")
        int autoConvertibleField;

        @Inject
        @ConfigProperty(name = "config.property")
        Object nonMatchingNonAutoConvertible;
    }
}

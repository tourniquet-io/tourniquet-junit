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

import static org.junit.Assert.assertNotNull;

import io.tourniquet.junit.jcr.rules.InMemoryContentRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InMemoryContentRepositoryBuilderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private Description description;

    private InMemoryContentRepositoryBuilder subject;

    @Before
    public void setUp() throws Exception {

        subject = new InMemoryContentRepositoryBuilder(folder);
    }

    @Test
    public void testBuild() throws Exception {

        final InMemoryContentRepository rule = subject.build();
        assertNotNull(rule);
    }

    @Test
    public void testEnableSecurity() throws Throwable {
        //prepare

        //act
        final InMemoryContentRepository rule = subject.withSecurityEnabled().build();

        rule.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                //without security, no user can be added
                rule.addUser("user1", "password");


            }
        }, description).evaluate();

    }

    @Test(expected = AssertionError.class)
    public void testDefaultSecurity() throws Throwable {
        //prepare
        final InMemoryContentRepository rule = subject.build();

        rule.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                //without security, no user can be added
                rule.addUser("user1", "password");


            }
        }, description).evaluate();

    }

}

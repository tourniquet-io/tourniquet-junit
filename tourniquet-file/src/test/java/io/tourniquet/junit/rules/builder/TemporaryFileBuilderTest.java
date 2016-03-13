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

package io.tourniquet.junit.rules.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.net.URL;

import io.tourniquet.junit.rules.TemporaryFile;
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

@RunWith(MockitoJUnitRunner.class)
public class TemporaryFileBuilderTest {

    private String fileName;

    @Mock
    private Description description;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private TemporaryFileBuilder subject;

    @Before
    public void setUp() throws Exception {

        fileName = "testFile.txt";
        subject = new TemporaryFileBuilder(folder, fileName);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testBuild() throws Exception {

        final TemporaryFile file = subject.build();
        assertNotNull(file);
    }

    @Test
    public void testFromClasspathResource_absolutePath_existing() throws Throwable {

        // prepare

        // act
        subject.fromClasspathResource("/io/tourniquet/junit/rules/builder/TemporaryFileBuilderTest_testContent.txt");

        // assert
        final TemporaryFile tempFile = subject.build();
        final Statement base = spy(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                final File f = tempFile.getFile();
                assertTrue(f.exists());
                assertEquals(4, f.length());

            }
        });
        tempFile.apply(base, description).evaluate();

        verify(base).evaluate();

    }

    @Test
    public void testFromResource() throws Throwable {

        // prepare
        final String resourceName = "TemporaryFileBuilderTest_testContent.txt";
        final URL resource = getClass().getResource(resourceName);

        // act
        subject.fromResource(resource);

        // assert
        final TemporaryFile tempFile = subject.build();
        final Statement base = spy(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                final File f = tempFile.getFile();
                assertTrue(f.exists());
                assertEquals(4, f.length());

            }
        });
        tempFile.apply(base, description).evaluate();

        verify(base).evaluate();
    }

    @Test(expected = AssertionError.class)
    public void testWithContent_noContent_fail() throws Throwable {

        // prepare

        // act
        subject.withContent();

        // assert
        final TemporaryFile tempFile = subject.build();
        final Statement base = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                final File f = tempFile.getFile();
                assertTrue(f.exists());
                assertEquals(4, f.length());

            }
        };
        tempFile.apply(base, description).evaluate();

    }

}

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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.zip.ZipFile;

import io.tourniquet.junit.rules.TemporaryFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 25.11.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ZipFileBuilderTest {

    /**
     * The class under test
     */
    @InjectMocks
    private ZipFileBuilder subject;

    @Mock
    private Description description;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {

        subject = new ZipFileBuilder(folder, "test.zip");
    }

    @Test
    public void testBuild() throws Exception {
        //prepare

        //act
        TemporaryFile file = subject.build();
        //assert
        assertNotNull(file);
    }

    @Test
    public void testBuild_withEmptyfolder() throws Throwable {
        //prepare
        subject.addFolder("/emptyFolder");

        //act
        final TemporaryFile file = subject.build();
        //assert
        assertNotNull(file);
        file.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                ZipFile zf = new ZipFile(file.getFile());

                //assert
                assertNotNull(zf.getEntry("emptyFolder"));

            }
        }, description).evaluate();
    }

    @Test
    public void testBuild_withFile() throws Throwable {
        //prepare
        subject.addClasspathResource("/folder/testfile.txt", "ZipFileBuilderTest_testContent.txt");

        //act
        final TemporaryFile file = subject.build();
        //assert
        assertNotNull(file);
        file.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                ZipFile zf = new ZipFile(file.getFile());

                //assert
                assertNotNull(zf.getEntry("folder/testfile.txt"));

            }
        }, description).evaluate();
    }

    @Test(expected = AssertionError.class)
    public void testAddClasspathResource_resourceMissing_fail() throws Exception {

        //prepare

        //act
        subject.addClasspathResource("/root", "nonexisting");

        //assert
    }

    @Test
    public void testAddClasspathResource_fluidApi() throws Exception {

        //prepare

        //act
        ZipFileBuilder builder = subject.addClasspathResource("/root", "ZipFileBuilderTest_testContent.txt");

        //assert
        assertSame(subject, builder);
    }

    @Test
    public void testAddFolder() throws Exception {
        //prepare

        //act
        ZipFileBuilder builder = subject.addFolder("pathToFolder");
        //assert
        assertSame(subject, builder);

    }

    @Test
    public void testAddResource() throws Exception {
        //prepare

        //act
        ZipFileBuilder builder = subject.addResource("pathToFolder",
                                                     getClass().getResource("ZipFileBuilderTest_testContent.txt"));
        //assert
        assertSame(subject, builder);

    }
}

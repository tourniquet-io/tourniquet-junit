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

package io.tourniquet.junit.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.tourniquet.junit.jcr.rules.builder.InMemoryContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.StandaloneContentRepositoryBuilder;
import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.rules.builder.TemporaryFileBuilder;
import io.tourniquet.junit.rules.ldap.builder.DirectoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TemporaryFolderBuilderTest {

    @Mock
    private Statement base;
    @Mock
    private Description description;

    private TemporaryFolderBuilder subject;

    @Before
    public void setUp() throws Exception {
        subject = new TemporaryFolderBuilder();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testAroundStandaloneContentRepository() throws Exception {
        final StandaloneContentRepositoryBuilder repository = subject.aroundStandaloneContentRepository();
        assertNotNull(repository);
    }

    @Test
    public void testAroundInMemoryContentRepository() throws Exception {
        final InMemoryContentRepositoryBuilder repository = subject.aroundInMemoryContentRepository();
        assertNotNull(repository);
    }

    @Test
    public void testAroundTempFile() throws Throwable {
        // act
        final TemporaryFileBuilder builder = subject.aroundTempFile("filename.txt");

        // assert
        assertNotNull(builder);

        // to verify the filename we must actually apply the rule to a statement as the filename is
        // only accessible via the file created by the rule. And as the file is deleted after the rule
        // has been applied, we must check the name of the file inside the statement.
        // to ensure the evaluate method is actually invoked, we verify the method invocation
        final TemporaryFile tempFile = builder.build();
        final Statement stmt = spy(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                assertEquals("filename.txt", tempFile.getFile().getName());
            }

        });
        tempFile.apply(stmt, description).evaluate();
        verify(stmt).evaluate();
    }

    @Test
    public void testAroundDirectory() throws Exception {
        //prepare

        //act
        DirectoryBuilder builder = subject.aroundDirectory();
        //assert
        assertNotNull(builder);

    }

    @Test
    public void testBuild() throws Exception {
        final TemporaryFolder tempFolder = subject.build();
        assertNotNull(tempFolder);
    }

}

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

package io.tourniquet.junit.rules;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;

public class TemporaryZipFileTest {

    /**
     * The class under test
     */
    @InjectMocks
    private TemporaryZipFile subject;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private Map<String, URL> content = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        subject = new TemporaryZipFile(folder, "testfile.zip", content);
    }
    @Test
    public void testCreateTempFile() throws Exception {
        //prepare
        content.put("emptyFolder", null);
        content.put("folder/file1.txt", getClass().getResource("TemporaryZipFileTest_content1.txt"));
        content.put("file2.txt", getClass().getResource("TemporaryZipFileTest_content2.txt"));

        //act
        File file = subject.createTempFile();

        //assert
        assertNotNull(file);
        assertTrue(file.exists());

        final ZipFile zf = new ZipFile(file);
        ZipAssert.assertZipContent(zf, "file2.txt", "content2");
        ZipAssert.assertZipContent(zf, "folder/file1.txt", "content1");
        ZipAssert.assertZipFolderExists(zf, "emptyFolder/");
    }


}

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

import java.util.zip.ZipFile;

import io.tourniquet.junit.rules.builder.TemporaryFileBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * Created by Gerald Muecke on 23.11.2015.
 */
public class TemporaryZipFileExample {

    public TemporaryFolder folder = new TemporaryFolder();

    //@formatter:off
    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.zip")
                                        .fromClasspathResource("exampleTestContent1.txt")
                                        .asZip()
                                        .addFolder("/emptyFolder")
                                        .addClasspathResource("/text1.txt","exampleTestContent1.txt")
                                        .addClasspathResource("/test/text2.txt","exampleTestContent2.txt")
                                        .build();

    //@formatter:on

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);

    @Test
    public void testZipStructure() throws Exception {
        //prepare

        //act
        ZipFile zf = new ZipFile(file.getFile());

        //assert
        ZipAssert.assertZipFolderExists(zf, "emptyFolder");
        ZipAssert.assertZipContent(zf, "exampleTestContent1.txt", "content1");
        ZipAssert.assertZipContent(zf, "text1.txt", "content1");
        ZipAssert.assertZipContent(zf, "test/text2.txt", "content2");

    }
}

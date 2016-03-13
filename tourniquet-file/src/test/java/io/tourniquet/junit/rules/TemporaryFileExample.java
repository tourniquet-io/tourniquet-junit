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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.InputStream;

import io.tourniquet.junit.rules.builder.TemporaryFileBuilder;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * Created by Gerald Muecke on 23.11.2015.
 */
public class TemporaryFileExample {

    public TemporaryFolder folder = new TemporaryFolder();

    //@formatter:off
    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.zip")
                                        .withContent()
                                        .fromClasspathResource("exampleTestContent1.txt").build();

    //@formatter:on

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);

    @Test
    public void testZipStructure() throws Exception {
        //prepare

        //act
        File f = file.getFile();

        //assert
        assertTrue(f.exists());
        try(InputStream is = f.toURI().toURL().openStream()){
            String content = IOUtils.toString(is);
            assertEquals("content1", content);
        }
    }
}

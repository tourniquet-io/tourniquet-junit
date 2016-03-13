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

package io.tourniquet.junit.pdf;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class BasePDFMatcherTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private Path validPdf;
    private Path invalidPdf;

    /**
     * The class under test
     */
    private BasePDFMatcher subject = new BasePDFMatcher();

    @Before
    public void setUp() throws Exception {
        File file = folder.newFile("test.pdf");
        try(InputStream is = BasePDFMatcherTest.class.getResourceAsStream("BasePDFMatcherTest.pdf")){
            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        this.validPdf = file.toPath();
        this.invalidPdf = folder.newFile("noPdf.txt").toPath();
    }

    @Test
    public void testIsPdf_valid_true() throws Exception {
        //prepare

        //act
        assertThat(PDF.of(validPdf), subject);

        //assert

    }


    @Test
    public void testIsPdf_invalid_false() throws Exception {
        //prepare

        //act
        assertThat(PDF.of(invalidPdf), IsNot.not(subject));

        //assert

    }


    @Test
    public void testIsPdf_noPath_false() throws Exception {
        //prepare

        //act
        assertThat(new Object(), IsNot.<Object>not(subject));

        //assert

    }
}

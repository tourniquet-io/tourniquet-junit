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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public class PDFTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    private File file;

    @Before
    public void setUp() throws Exception {
        this.file = folder.newFile("test.pdf");
    }

    @Test
    public void testOf_file() throws Exception {

        PDF pdf = PDF.of(this.file);

        assertNotNull(pdf);
        assertNotNull(pdf.toDataSource());
        assertEquals(this.file.toString(),pdf.toString());
        try(InputStream is = pdf.openStream()){
            assertNotNull(is);
        }

    }

    @Test
    public void testOf_path() throws Exception {
        PDF pdf = PDF.of(this.file.toPath());

        assertNotNull(pdf);
        assertNotNull(pdf.toDataSource());
        assertEquals(this.file.toPath().toString(),pdf.toString());
        try(InputStream is = pdf.openStream()){
            assertNotNull(is);
        }
    }

    @Test
    public void testOf_stringPath() throws Exception {
        PDF pdf = PDF.of(this.file.getAbsolutePath());

        assertNotNull(pdf);
        assertNotNull(pdf.toDataSource());
        try(InputStream is = pdf.openStream()){
            assertNotNull(is);
        }
    }

    @Test
    public void testOf_url() throws Exception {
        PDF pdf = PDF.of(this.file.toURL());

        assertNotNull(pdf);
        assertNotNull(pdf.toDataSource());
        try(InputStream is = pdf.openStream()){
            assertNotNull(is);
        }
    }

    @Test
    public void testOf_bytedata() throws Exception {
        PDF pdf = PDF.of(new byte[0]);

        assertNotNull(pdf);
        assertNotNull(pdf.toDataSource());
        try(InputStream is = pdf.openStream()){
            assertNotNull(is);
        }
    }

    @Test
    public void testOf_inputstream() throws Exception {
        PDF pdf = PDF.of(new ByteArrayInputStream(new byte[0]));

        assertNotNull(pdf);
        assertNotNull(pdf.toDataSource());
        try(InputStream is = pdf.openStream()){
            assertNotNull(is);
        }
    }
}

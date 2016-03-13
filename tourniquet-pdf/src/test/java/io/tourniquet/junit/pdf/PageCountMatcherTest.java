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

import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

/**
 *
 */
public class PageCountMatcherTest {

    private URL resource(final String resource) {

        return PDFMatchersTest.class.getResource(resource);
    }

    @Test
    public void testMatchesPDF() throws Exception {
        //prepare

        PDF pdf = PDF.of(resource("BasePDFMatchers_hasPagesTest.pdf"));
        //act & assert
        assertThat(pdf, new PageCountMatcher(3));
    }
}

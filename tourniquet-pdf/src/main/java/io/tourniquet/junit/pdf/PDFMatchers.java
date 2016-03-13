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

import org.hamcrest.Matcher;

/**
 * Library containing hamcrest matchers for operating with PDF files.
 */
public final class PDFMatchers {

    private PDFMatchers() {

    }

    /**
     * Creates a matcher that verifies, if a file identified by a {@link java.nio.file.Path} is a valid PDF document.
     *
     * @return a matcher verifying a file to be a PDF
     */
    public static Matcher<? super PDF> isPdf() {

        return new BasePDFMatcher();
    }

    /**
     * Creates a matcher that verifies, if a PDF has a specific number of pages
     *
     * @param pageCount
     *         the number of expected pages
     *
     * @return a matcher verifying the pages of a PDF
     */
    public static Matcher<? super PDF> hasPages(final int pageCount) {

        return new PageCountMatcher(pageCount);
    }

    /**
     * Creates a matcher for verifying if a document is valid against a PDF/A conformance level.
     *
     * @param conformanceLevel
     *         the expected conformance level
     *
     * @return a matcher verifying the PDF/A conformance
     */
    public static Matcher<? super PDF> conformsTo(final PDFALevel conformanceLevel) {

        return new PDFAConformanceMatcher(conformanceLevel);
    }

}

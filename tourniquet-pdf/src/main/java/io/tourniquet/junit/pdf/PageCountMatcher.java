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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.hamcrest.Description;

/**
 * Matcher to verify a PDF document has a specifc number of pages.
 */
class PageCountMatcher extends BasePDFMatcher {

    private final int pageCount;
    private int actualNumPages;

    public PageCountMatcher(final int pageCount) {

        this.pageCount = pageCount;
    }

    @Override
    protected boolean matchesPDF(final PDDocument doc) {

        this.actualNumPages = doc.getNumberOfPages();
        return doc.getNumberOfPages() == pageCount;
    }

    @Override
    public void describeTo(final Description description) {

        description.appendText(pageCount + " pages");
    }

    @Override
    public void describeMismatch(Object item, Description description) {

        description.appendText(this.actualNumPages + " pages");
    }
}

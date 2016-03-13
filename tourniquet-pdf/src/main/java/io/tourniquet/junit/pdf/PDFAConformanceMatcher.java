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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.parser.PreflightParser;
import org.hamcrest.Description;
import org.slf4j.Logger;

/**
 * Matcher to verify a PDF document conforms to a specific PDF/A level.
 */
class PDFAConformanceMatcher extends BasePDFMatcher {

    private static final Logger LOG = getLogger(PDFAConformanceMatcher.class);

    private final PDFALevel conformanceLevel;
    private ValidationResult validationResult;

    public PDFAConformanceMatcher(final PDFALevel conformanceLevel) {

        this.conformanceLevel = conformanceLevel;
    }

    @Override
    public void describeTo(Description description) {

        description.appendText("a valid " + conformanceLevel.getFormat().toString() + " document");
    }

    @Override
    protected boolean matches(PDF pdf) {

        try {
            final PreflightParser parser = new PreflightParser(pdf.toDataSource());
            parser.parse(conformanceLevel.getFormat());
            final PreflightDocument doc = parser.getPreflightDocument();
            doc.validate();
            final ValidationResult result = doc.getResult();
            this.validationResult = result;
            return result.isValid();
        } catch (IOException e) {
            LOG.debug("Could not read PDF", e);
            return false;
        }
    }

    @Override
    public void describeMismatch(Object item, Description description) {

        description.appendText("document does not conform to " + conformanceLevel.toString() + ", because:\n");
        for (ValidationResult.ValidationError error : this.validationResult.getErrorsList()) {
            description.appendText("-").appendText(error.getDetails()).appendText("\n");
        }
    }
}

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

package io.tourniquet.pageobjects;

import static io.tourniquet.selenium.SeleniumContext.currentDriver;
import static io.tourniquet.pageobjects.WaitPredicates.documentReady;
import static io.tourniquet.pageobjects.WaitPredicates.elementDisplayed;
import static io.tourniquet.pageobjects.WaitPredicates.elementNotDisplayed;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Factory class for common waits
 */
public final class ActiveWaits {

    private ActiveWaits(){}

    /**
     * Active wait for an element not to be displayed
     * @param context
     *  the search context to locate the element
     * @param locator
     *  the locator used to find the object
     * @return
     *  an active wait that either waits wait the element is gone or not displayed anymore
     */
    public static ActiveWait untilElementDisplayed(SearchContext context, By locator) {
        return timeout -> fluentWaitForElement(context, timeout).until(elementDisplayed(context, locator));
    }

    /**
     * Active wait for an element to be displayed
     * @param context
     *  the search context to locate the element
     * @param locator
     *  the locator used to find the object
     * @return
     *  an active wait that locates waits for the element to exist and to be displayed
     */
    public static ActiveWait untilElementNotDisplayed(SearchContext context, By locator) {
        return timeout -> fluentWaitForElement(context, timeout).until(elementNotDisplayed(context, locator));
    }

    /**
     * Creates a fluent wait instance that waits for the presence of an element in the search context. The timeout for
     * the wait uses a milli-second precision with a polling interval of 1/10 of the timeout.
     * @param context
     *  the search context to find the element to wait for
     * @param timeout
     *  the timeout to wait before timing out
     * @return
     *  a fluent wait instance
     */
    private static FluentWait<SearchContext> fluentWaitForElement(final SearchContext context,
                                                                  final Duration timeout) {

        return new FluentWait<>(context).ignoring(NoSuchElementException.class)
                                        .pollingEvery(timeout.toMillis() / 10, TimeUnit.MILLISECONDS)
                                        .withTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Active wait that waits wait the document is rendered which is indicated by the document's ready-state.
     * @return
     *  active wait for document rendering complete
     */
    public static ActiveWait untilDocumentReady() {
        return timeout -> currentDriver().ifPresent(driver -> new WebDriverWait(driver, timeout.getSeconds(), 50).until(
                documentReady()));
    }


}

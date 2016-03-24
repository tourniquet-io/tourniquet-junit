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

import static io.tourniquet.pageobjects.ActiveWaits.untilDocumentReady;
import static io.tourniquet.pageobjects.SeleniumContext.getTimeoutFor;
import static io.tourniquet.pageobjects.TimeoutProvider.RENDER_TIMEOUT;
import static io.tourniquet.tx.TransactionHelper.addTransactionSupport;

import java.util.Optional;

import io.tourniquet.tx.TransactionSupport;
import org.openqa.selenium.WebElement;

/**
 * Loader of a page instance. The page loader adds transaction support to the instance to measure the execution of
 * transactional methods.
 */
public final class PageLoader {

    private PageLoader() {

    }

    /**
     * Loads a page with optional transaction support.
     *
     * @param pageType
     *         the page type to instantiate
     * @param <T>
     *         the type of the page
     *
     * @return the page instance of the object model
     */
    public static <T extends Page> T loadPage(Class<T> pageType) {

        try {
            T page = pageType.newInstance();
            if (TransactionSupport.class.isAssignableFrom(pageType)) {
                page = addTransactionSupport((TransactionSupport) page);
            }
            return page;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("Page " + pageType.getName() + " can not be loaded", e);
        }
    }

    /**
     * Loads the page into the current webdriver. The method waits for the document to be completely loaded and
     * rendered.<br> If no {@link SeleniumContext} is initialized, an {@link IllegalStateException} is thrown.
     *
     * @param page
     *         the page handle to load
     */
    public static void loadPage(Page page) {

        SeleniumContext.currentDriver().map(driver -> {
            Optional.ofNullable(page.getClass().getAnnotation(Locator.class))
                    .flatMap(l -> l.by().locate(l.value()))
                    .ifPresent(WebElement::click);
            WaitChain.wait(untilDocumentReady()).orTimeoutAfter(getTimeoutFor(RENDER_TIMEOUT));
            return Void.TYPE;
        }).orElseThrow(() -> new IllegalStateException("Context not initialized"));
    }
}

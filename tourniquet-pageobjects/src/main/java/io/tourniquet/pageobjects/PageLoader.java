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
import static io.tourniquet.pageobjects.Timeouts.getTimeout;
import static io.tourniquet.junit.util.TypeUtils.isAbstract;
import static io.tourniquet.selenium.TimeoutProvider.RENDER_TIMEOUT;
import static io.tourniquet.tx.TransactionHelper.addTransactionSupport;

import java.util.Optional;

import io.tourniquet.tx.TransactionSupport;
import net.sf.cglib.proxy.Enhancer;
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
    @SuppressWarnings("unchecked")
    public static <T extends Page> T loadPage(Class<T> pageType) {

        try {
            T page = newInstance(pageType);
            if (TransactionSupport.class.isAssignableFrom(pageType)) {
                page = addTransactionSupport((TransactionSupport) page);
            }
            return page;

        } catch (InstantiationException | IllegalAccessException e) {
            throw new AssertionError("Page " + pageType.getName() + " can not be loaded", e);
        }
    }

    /**
     * Creates a new instance of the specified page. If the page type denotes an abstract class, a dynamic proxy (using
     * CGLib) is created, providing injected implementations for the abstract methods.
     *
     * @param pageType
     *         the type of the page to create
     * @param <T>
     *         the type of the page to create
     *
     * @return an instance of the page
     *
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    private static <T extends Page> T newInstance(final Class<T> pageType)
            throws InstantiationException, IllegalAccessException {

        if (isAbstract(pageType)) {
            return (T) Enhancer.create(pageType, new Class[0], new DynamicElementGroupInterceptor(pageType));
        }
        return pageType.newInstance();
    }

    /**
     * Loads the page into the current webdriver. The method waits for the document to be completely loaded and
     * rendered.<br> If no {@link io.tourniquet.selenium.SeleniumContext} is initialized, an {@link
     * IllegalStateException} is thrown.
     *
     * @param page
     *         the page handle to load
     */
    public static void loadPage(Page page) {

        final Optional<Locator> locator = Optional.ofNullable(page.getClass().getAnnotation(Locator.class));
        locator.flatMap(l -> l.by().locate(l.value())).ifPresent(WebElement::click);
        WaitChain.wait(untilDocumentReady()).orTimeoutAfter(getTimeout(locator, RENDER_TIMEOUT));

    }

}

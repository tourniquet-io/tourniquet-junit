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

import static io.tourniquet.tx.TransactionHelper.getClassTxName;

import java.util.Optional;

import io.tourniquet.tx.TransactionSupport;

/**
 * Interface to declare a page of an application
 */
public interface Page extends ElementGroup {

    /**
     * Default implementation navigates to the url of the page specified by locator annotation. If the page as another
     * mechanism of navigating to it, this method must be overriden.
     */
    default void loadPage() {

        PageLoader.loadPage(this);
    }

    /**
     * Navigates to a specific page of the page object model.
     *
     * @param pageType
     *         the type of the page
     * @param <T>
     *         the class declaring the page
     *
     * @return an instance of the page
     */
    static <T extends Page> T navigateTo(Class<T> pageType) {

        final T page = PageLoader.loadPage(pageType);
        final Optional<TransactionSupport> tx = TransactionSupport.class.isAssignableFrom(pageType)
                                                ? Optional.of((TransactionSupport) page)
                                                : Optional.empty();
        tx.ifPresent(ts -> getClassTxName(pageType).ifPresent(ts::txBegin));
        try {
            page.loadPage();
        } finally {
            tx.ifPresent(ts -> getClassTxName(pageType).ifPresent(ts::txEnd));
        }

        page.locateElements();
        return page;

    }

}

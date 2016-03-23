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

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;

/**
 * A set of common predicates that can be used for waiting
 */
public final class WaitPredicates {

    private WaitPredicates(){}


    /**
     * Wait until the element is displayed.
     * @param context
     *  the context in which the element should be searched for
     * @param locator
     *  the locator to identify the element in the search context
     */
    public static Predicate<SearchContext> elemenDisplayed(SearchContext context, By locator){
        return c -> context.findElement(locator).isDisplayed();
    }

    /**
     * Wait until the element is displayed. The search context is the entire page.
     * @param locator
     *  the locator to identify the element in the search context
     */
    public static Predicate<SearchContext> elemenDisplayed(By locator){
        return c -> elemenDisplayed(c, locator).apply(c);
    }

    /**
     * Wait until the element is not displayed anymore
     * @param context
     *  the context in which the element should be searched for
     * @param locator
     *  the locator to identify the element in the search context
     */
    public static Predicate<SearchContext> elementNotDisplayed(SearchContext context, By locator){
        return c -> !context.findElement(locator).isDisplayed();
    }

    /**
     * Wait until the element is not displayed anymore. The search context is the entire page.
     * @param locator
     *  the locator to identify the element in the search context
     */
    public static Predicate<SearchContext> elementNotDisplayed(By locator){
        return c -> elementNotDisplayed(c, locator).apply(c);
    }

    /**
     * Wait until the page is rendered
     */
    public static  Predicate<WebDriver> documentReady() {
            return  d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState"));
    }

}

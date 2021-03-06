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

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import java.util.function.Function;

import io.tourniquet.selenium.SeleniumContext;
import io.tourniquet.selenium.TimeoutProvider;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * A locator annotation to declare how a page or an element can be addressed
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Locator {

    /**
     * The locator string to specify which element should be located
     *
     * @return
     *  the locator-specific id to locate the element
     */
    String value();

    /**
     * Specifies the locator type
     *
     * @return
     *  the locator strategy how the element should be located
     */
    ByLocator by() default ByLocator.URL;

    /**
     * Timeout in seconds to wait for the element to be present
     *
     * @return
     *  timeout in seconds
     */
    int timeout() default TimeoutProvider.DEFAULT_TIMEOUT_INT;

    /**
     * Key for making the timeout configurable. If a {@link TimeoutProvider}
     * is used, the timeout for this locator can be configured.
     * @return
     *  the key for determining the configured timeout
     */
    String timeoutKey() default "";

    enum ByLocator {
        /**
         * URL are only for pages
         */
        URL(null) {
            @Override
            public Optional<WebElement> locate(String selector) {
                currentDriver().navigate().to(SeleniumContext.resolve(selector));
                return Optional.empty();
            }
        },
        ID(By::id),
        LINK_TEXT(By::linkText),
        PARTIAL_LINK_TEXT(By::partialLinkText),
        NAME(By::name),
        TAG(By::tagName),
        XPATH(By::xpath),
        CLASS(By::className),
        CSS(By::cssSelector);

        private transient final Optional<Function<String, By>> mapper;

        ByLocator(Function<String, By> mapper) {
            this.mapper = Optional.ofNullable(mapper);
        }

        /**
         * Locates the web element on the current page using the appropriate locator strategy
         *
         * @param selector
         *         the selector for the element to select
         *
         * @return a reference to the element
         */
        public Optional<WebElement> locate(String selector) {
            return locate(currentDriver(), selector);
        }

        /**
         * Locates the web element on the current page using the appropriate locator strategy
         *
         * @param selector
         *         the selector for the element to select
         *
         * @return a reference to the element
         */
        public Optional<WebElement> locate(SearchContext parent, String selector) {
            return mapper.map(by -> parent.findElement(by.apply(selector)));
        }

        /**
         * Transforms the ByLocator to a Selenium {@link org.openqa.selenium.By} literal.
         * @param selector
         *  the selector String
         * @return
         *  the selenium By literal
         */
        public By withSelector(String selector) {
            return mapper.map(by -> by.apply(selector))
                         .orElseThrow(() -> new IllegalArgumentException("Not supported for " + this.name()));
        }
    }
}

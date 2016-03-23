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

import static io.tourniquet.pageobjects.SeleniumContext.currentDriver;
import static io.tourniquet.pageobjects.WaitPredicates.documentReady;
import static io.tourniquet.pageobjects.WaitPredicates.elemenDisplayed;
import static io.tourniquet.pageobjects.WaitPredicates.elementNotDisplayed;

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

    public static ActiveWait untilElementNotDisplayed(SearchContext context, By locator) {
        return timeout -> new FluentWait<>(context).ignoring(NoSuchElementException.class)
                                                   .withTimeout(timeout.getSeconds(), TimeUnit.SECONDS)
                                                   .until(elementNotDisplayed(context, locator));
    }

    public static ActiveWait untilElementDisplayed(SearchContext context, By locator) {
        return timeout -> new FluentWait<>(context).ignoring(NoSuchElementException.class)
                                                   .withTimeout(timeout.getSeconds(), TimeUnit.SECONDS)
                                                   .until(elemenDisplayed(context, locator));
    }

    public ActiveWait untilDocumentReady() {
        return timeout -> currentDriver().ifPresent(driver -> new WebDriverWait(driver, timeout.getSeconds(), 50).until(
                documentReady()));
    }


}

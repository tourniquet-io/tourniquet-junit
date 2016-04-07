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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import com.google.common.base.Predicate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WaitPredicatesTest {

    @Mock
    private SearchContext context;

    @Mock
    private SearchContext anyContext;

    @Mock
    private By by;

    @Mock
    private WebElement webElement;


    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Test
    public void testElementDisplayed_contextLocator() throws Exception {
        //prepare
        when(context.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        //act
        Predicate<SearchContext> predicate = WaitPredicates.elementDisplayed(context, by);

        //assert
        assertNotNull(predicate);
        assertTrue(predicate.apply(anyContext));

    }

    @Test
    public void testElementDisplayed_locator() throws Exception {
        //prepare
        when(context.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        //act
        Predicate<SearchContext> predicate = WaitPredicates.elementDisplayed(by);

        //assert
        assertNotNull(predicate);
        assertTrue(predicate.apply(context));
    }

    @Test
    public void testElementNotDisplayed_contextLocator() throws Exception {
        //prepare
        when(context.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);

        //act
        Predicate<SearchContext> predicate = WaitPredicates.elementNotDisplayed(context, by);

        //assert
        assertNotNull(predicate);
        assertTrue(predicate.apply(anyContext));
    }

    @Test
    public void testElementNotDisplayed_locator() throws Exception {
        //prepare
        when(context.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);

        //act
        Predicate<SearchContext> predicate = WaitPredicates.elementNotDisplayed(by);

        //assert
        assertNotNull(predicate);
        assertTrue(predicate.apply(context));
    }

    @Test
    public void testDocumentReady() throws Exception {
        //prepare
        when(((JavascriptExecutor)webDriver).executeScript("return document.readyState")).thenReturn("complete");

        //act
        Predicate<WebDriver> predicate = WaitPredicates.documentReady();

        //assert
        assertNotNull(predicate);
        assertTrue(predicate.apply(webDriver));
    }
}

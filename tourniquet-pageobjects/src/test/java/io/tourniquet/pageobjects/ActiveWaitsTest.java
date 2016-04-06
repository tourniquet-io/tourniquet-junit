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
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ActiveWaitsTest {

    @Mock
    private SearchContext searchContext;

    @Mock
    private By by;

    @Mock
    private WebElement webElement;

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @After
    public void tearDown() throws Exception {
        SeleniumContext.currentContext().ifPresent(SeleniumContext::destroy);
    }

    @Test
    public void testUntilElementDisplayed_found() throws Exception {
        //prepare
        when(searchContext.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        //act
        ActiveWait wait = ActiveWaits.untilElementDisplayed(searchContext, by);

        //assert
        assertNotNull(wait);
        wait.wait(Duration.ZERO);

    }

    @Test(expected = TimeoutException.class)
    public void testUntilElementDisplayed_timeout() throws Exception {
        //prepare
        when(searchContext.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);

        //act
        ActiveWait wait = ActiveWaits.untilElementDisplayed(searchContext, by);

        //assert
        assertNotNull(wait);
        wait.wait(Duration.ZERO);

    }

    @Test
    public void testUntilElementNotDisplayed_notDisplayed() throws Exception {
        //prepare
        when(searchContext.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);

        //act
        ActiveWait wait = ActiveWaits.untilElementNotDisplayed(searchContext, by);

        //assert
        assertNotNull(wait);
        wait.wait(Duration.ZERO);
    }
    @Test(expected = TimeoutException.class)
    public void testUntilElementNotDisplayed_timeout() throws Exception {
        //prepare
        when(searchContext.findElement(by)).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        //act
        ActiveWait wait = ActiveWaits.untilElementNotDisplayed(searchContext, by);

        //assert
        assertNotNull(wait);
        wait.wait(Duration.ZERO);
    }

    @Test
    public void testUntilDocumentReady_ready() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();
        when(((JavascriptExecutor)webDriver).executeScript("return document.readyState")).thenReturn("complete");

        //act
        ActiveWait wait = ActiveWaits.untilDocumentReady();

        //assert
        assertNotNull(wait);
        wait.wait(Duration.ZERO);
    }

    @Test(expected = TimeoutException.class)
    public void testUntilDocumentReady_timeout() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();
        when(((JavascriptExecutor)webDriver).executeScript("return document.readyState")).thenReturn("loading");

        //act
        ActiveWait wait = ActiveWaits.untilDocumentReady();

        //assert
        assertNotNull(wait);
        wait.wait(Duration.ZERO);
    }
}

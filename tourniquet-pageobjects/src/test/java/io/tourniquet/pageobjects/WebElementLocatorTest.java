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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WebElementLocatorTest {

    public static final Duration TIMEOUT_1S = Duration.ofSeconds(1);
    public static final Duration TIMEOUT_10S = Duration.ofSeconds(10);
    @Mock
    private SearchContext searchContext;

    @Mock
    private WebElement webElement;

    @Mock
    private Locator locator;

    @Mock
    private TimeoutProvider timeoutProvider;

    @Rule
    public SeleniumTestContext selenium = new SeleniumTestContext();


    private SeleniumControl ctx;




    @Test
    public void testLocate() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(30);
        when(selenium.getMockDriver().findElement(By.id("testId"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);

        //act
        WebElement element = selenium.execute(() -> WebElementLocator.locate(locator));

        //assert
        assertNotNull(element);
        assertEquals(webElement, element);
    }

    @Test
    public void testLocate_context() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(30);
        when(webElement.isDisplayed()).thenReturn(true);
        when(searchContext.findElement(By.id("testId"))).thenReturn(webElement);

        //act
        WebElement element = WebElementLocator.locate(searchContext, locator);

        //assert
        assertNotNull(element);
        assertEquals(webElement, element);
    }

    @Test(expected = TimeoutException.class)
    public void testLocate_context_timeout() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(1);
        when(locator.timeoutKey()).thenReturn("");
        when(webElement.isDisplayed()).thenReturn(false);
        when(searchContext.findElement(By.id("testId"))).thenReturn(webElement);

        //act
        Instant start = Instant.now();
        try {
            WebElementLocator.locate(searchContext, locator);
        } finally {
            Duration dur = Duration.between(start, Instant.now());
            assertTrue(dur.compareTo(Duration.ofMillis(950)) > 0);
            assertTrue(dur.compareTo(Duration.ofMillis(1250)) < 0);
        }
    }

    @Test(expected = TimeoutException.class)
    public void testLocate_context_customTimeout() throws Throwable {
        //prepare
        when(locator.by()).thenReturn(Locator.ByLocator.ID);
        when(locator.value()).thenReturn("testId");
        when(locator.timeout()).thenReturn(1);
        when(locator.timeoutKey()).thenReturn("customTimeout");
        when(webElement.isDisplayed()).thenReturn(false);
        when(searchContext.findElement(By.id("testId"))).thenReturn(webElement);
        final SeleniumContext ctx = new SeleniumContext(() -> selenium.getMockDriver());
        ctx.setTimeoutProvider(timeoutProvider);
        ctx.init();
        when(timeoutProvider.getTimeoutFor("customTimeout")).thenReturn(Duration.ofMillis(500));

        //act
        Instant start = Instant.now();
        try {
            WebElementLocator.locate(searchContext, locator);
        } finally {
            SeleniumContext.currentContext().ifPresent(SeleniumContext::destroy);
            Duration dur = Duration.between(start, Instant.now());
            System.out.printf("timed out after %s ms\n", dur.toMillis());
            assertTrue(dur.compareTo(Duration.ofMillis(450)) > 0);
            assertTrue(dur.compareTo(Duration.ofMillis(750)) < 0);
        }
    }

    @Test
    public void testWaitForElement_context() throws Throwable {

        when(searchContext.findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertNotNull(WebElementLocator.waitForElement(searchContext, By.id("test"), TIMEOUT_10S));
    }

    @Test
    public void testWaitForElement_noDriver_noElement() throws Throwable {

        when(selenium.getMockDriver().findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        assertFalse(WebElementLocator.waitForElement(By.id("test"), TIMEOUT_10S).isPresent());
    }

    @Test
    public void testWaitForElement_noContext() throws Throwable {

        when(selenium.getMockDriver().findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(true);
        selenium.execute(() -> {
            assertTrue(WebElementLocator.waitForElement(By.id("test"), TIMEOUT_10S).isPresent());
            return null;
        });
    }

    @Test(expected = TimeoutException.class)
    public void testWaitForElement_noContext_timeout() throws Throwable {

        when(selenium.getMockDriver().findElement(By.id("test"))).thenReturn(webElement);
        when(webElement.isDisplayed()).thenReturn(false);
        selenium.execute(() -> {
            assertTrue(WebElementLocator.waitForElement(By.id("test"), TIMEOUT_1S).isPresent());
            return null;
        });
    }


}

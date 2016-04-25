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

import static io.tourniquet.pageobjects.Locator.ByLocator.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import io.tourniquet.measure.ResponseTime;
import io.tourniquet.measure.ResponseTimeCollector;
import io.tourniquet.measure.ResponseTimes;
import io.tourniquet.selenium.SeleniumContext;
import io.tourniquet.selenium.TimeoutProvider;
import io.tourniquet.tx.Transaction;
import io.tourniquet.tx.TransactionSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

@RunWith(MockitoJUnitRunner.class)
public class PageLoaderTest {

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock
    private TimeoutProvider timeoutProvider;

    private ResponseTimeCollector rtc = new ResponseTimeCollector();
    private AtomicReference<ResponseTime> rtEndRef = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {

        ResponseTimes.current().onMeasureEnd(responseTime -> rtEndRef.set(responseTime));
        rtc.startCollecting();
    }

    @After
    public void tearDown() throws Exception {

        rtc.stopCollecting();
        ResponseTimes.current().onMeasureEnd(null);
        ResponseTimes.current().clear();
        SeleniumContext.currentContext().ifPresent(SeleniumContext::destroy);
    }

    @Test
    public void testLoadPage() throws Exception {
        //act
        TestPage page = PageLoader.loadPage(TestPage.class);
        //assert
        assertNotNull(page);
    }

    @Test
    public void testLoadTransactionalPage() throws Exception {
        //act
        TxPage page = PageLoader.loadPage(TxPage.class);
        //assert
        assertNotNull(page);
        page.transactionalOp();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertEquals("transactionalOp", rt.getTransaction());
    }

    @Test(expected = IllegalStateException.class)
    public void testLoadPageInstance_noInstance_exception() throws Exception {
        PageLoader.loadPage(PageLoader.loadPage(TestPage.class));
    }

    @Test(expected = TimeoutException.class)
    public void testLoadPageInstance_timeout() throws Exception {
        //prepare
        SeleniumContext ctx = new SeleniumContext(() -> webDriver);
        ctx.setTimeoutProvider(timeoutProvider);
        ctx.init();
        when(((JavascriptExecutor)webDriver).executeScript(anyString())).thenReturn("");
        when(timeoutProvider.getTimeoutFor(TimeoutProvider.RENDER_TIMEOUT)).thenReturn(Optional.of(Duration.ZERO));

        //act

        PageLoader.loadPage(PageLoader.loadPage(TestPage.class));
        //assert
    }


    @Test
    public void testLoadPageInstance_noTimeout() throws Exception {
        //prepare
        SeleniumContext ctx = new SeleniumContext(() -> webDriver);
        ctx.init();
        when(((JavascriptExecutor)webDriver).executeScript(anyString())).thenReturn("complete");

        //act

        PageLoader.loadPage(PageLoader.loadPage(TestPage.class));
        //assert
        verify((JavascriptExecutor)webDriver).executeScript(anyString());
    }

    @Test(expected = AssertionError.class)
    public void testLoadPage_privateConstructor() throws Exception {

        PageLoader.loadPage(PrivateConstructorPage.class);
    }

    @Test(expected = AssertionError.class)
    public void testLoadPage_parameterConstructor() throws Exception {

        PageLoader.loadPage(ParameterConstructorPage.class);
    }

    @Test
    public void testAbstractPage_Click() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();
        WebElement element = mock(WebElement.class);
        when(webDriver.findElement(By.id("someButton"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        //act
        AbstractPage page = PageLoader.loadPage(AbstractPage.class);

        //assert
        assertNotNull(page);
        WebElement e = page.pressButton();
        verify(element).click();
        assertNotNull(e);
        assertEquals(element, e);
        assertEquals("testpage", page.toString());
    }

    @Test
    public void testAbstractPage_SubmitForm() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();
        WebElement element = mock(WebElement.class);
        when(webDriver.findElement(By.id("someForm"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.getTagName()).thenReturn("form");

        //act
        AbstractPage page = PageLoader.loadPage(AbstractPage.class);

        //assert
        assertNotNull(page);
        page.submitForm();
        verify(element).submit();
        assertEquals("testpage", page.toString());
    }

    @Test
    public void testAbstractPage_sendKeys() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();
        WebElement element = mock(WebElement.class);
        when(webDriver.findElement(By.id("someInput"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        //act
        AbstractPage page = PageLoader.loadPage(AbstractPage.class);

        //assert
        assertNotNull(page);
        AbstractPage fluentPage = page.enterValue("text");
        verify(element).clear();
        verify(element).sendKeys("text");
        assertNotNull(fluentPage);
        assertEquals(page, fluentPage);
        assertEquals("testpage", fluentPage.toString());
    }

    @Test(expected = NoSuchMethodException.class)
    public void testAbstractPage_nonInjectedMethod() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();

        //act
        AbstractPage page = PageLoader.loadPage(AbstractPage.class);

        //assert
        assertNotNull(page);
        //this method will not be implemented
        page.unimplemented();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbstractPage_twoManyArgs() throws Exception {
        //prepare
        new SeleniumContext(() -> webDriver).init();
        WebElement element = mock(WebElement.class);
        when(webDriver.findElement(By.id("someInput"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);

        //act
        AbstractPage page = PageLoader.loadPage(AbstractPage.class);

        //assert
        assertNotNull(page);
        //this method is not supported
        page.twoParamsMethod("text","2ndParam");
    }

    // -------------- Test Page Model ------------------------

    public static class TestPage implements Page {

    }

    public static class ParameterConstructorPage implements Page {

        public ParameterConstructorPage(String someParam) {

        }
    }

    public static class PrivateConstructorPage implements Page {

        private PrivateConstructorPage() {

        }
    }

    public static class TxPage implements Page, TransactionSupport {

        @Transaction
        public void transactionalOp() {

        }
    }

    public static abstract class AbstractPage implements Page {

        @Locator(by = ID, value="someForm")
        public abstract void submitForm();

        @Locator(by = ID, value="someButton")
        public abstract WebElement pressButton();

        @Locator(by = ID, value="someInput")
        public abstract AbstractPage enterValue(String someText);

        @Locator(by = ID, value="someInput")
        public abstract AbstractPage twoParamsMethod(String someText, String secondParam);

        public abstract void unimplemented();

        @Override
        public String toString() {

            return "testpage";
        }
    }


}

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
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumContextTest {

    @Mock
    private Description description;

    @Mock
    private WebDriver webDriver;

    /**
     * The class under test
     */
    private SeleniumContext subject;

    @Before
    public void setUp() throws Exception {
        subject = new SeleniumContext(() -> webDriver);
    }

    private void assertContextInitialized() {

        assertNotNull(subject.getDriver());
        assertNotNull(SeleniumContext.currentContext());
        assertNotNull(SeleniumContext.currentDriver());
    }

    private void assertContextDestroyed() {

        try {
            subject.getDriver();
            throw new AssertionError("IllegalStateException expected");
        } catch (IllegalStateException x){}
        try {
            SeleniumContext.currentContext();
            throw new AssertionError("IllegalStateException expected");
        } catch (IllegalStateException x){}
        try {
            SeleniumContext.currentDriver();
            throw new AssertionError("IllegalStateException expected");
        } catch (IllegalStateException x){}
    }

    @Test
    public void testInit_destroy() throws Exception {

        subject.init();
        try {
            assertContextInitialized();
        } finally {
            subject.destroy();
            assertContextDestroyed();
            verify(webDriver).quit();
        }
    }

    @Test
    public void testInit_destroy_noCloseDriver() throws Exception {

        subject.init();
        try {
            assertContextInitialized();
        } finally {
            subject.destroy(false);
            assertContextDestroyed();
            verify(webDriver, times(0)).quit();
        }
    }


    @Test(expected = IllegalStateException.class)
    public void testCurrentContext_outsideTest_exception() throws Exception {
        SeleniumContext.currentContext();
    }

    @Test(expected = IllegalStateException.class)
    public void testCurrentDriver_outsideTest_exception() throws Exception {
        SeleniumContext.currentDriver();
    }

    @Test(expected = IllegalStateException.class)
    public void testGetDriver_outsideTest_exception() throws Exception {
        subject.getDriver();
    }

    @Test
    public void testSetGetBaseUrl() throws Exception {
        subject.setBaseUrl("http://localhost");
        assertEquals("http://localhost", subject.getBaseUrl());
    }

    @Test(expected = NullPointerException.class)
    public void testSetBaseUrl_null() throws Exception {
        subject.setBaseUrl(null);
    }

    @Test(expected = IllegalStateException.class)
    public void testResolve_outsideTest_exception() throws Exception {
        SeleniumContext.resolve("relativePath");
    }

    @Test
    public void testResolve_insideTest_baseUrlTrailingSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl/";
        String relPath = "relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    @Test
    public void testResolve_insideTest_relPathLeadingSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl";
        String relPath = "/relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    @Test
    public void testResolve_insideTest_baseUrlTrailing_and_relPathLeadingSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl/";
        String relPath = "/relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    @Test
    public void testResolve_insideTest_noSlash() throws Throwable {
        //prepare
        String basePath = "http://myBaseUrl";
        String relPath = "relativePath";
        String expected = "http://myBaseUrl/relativePath";

        testResolvePathInsideTest(basePath, relPath, expected);

    }

    private void testResolvePathInsideTest(final String basePath, final String relPath, final String expected)
            throws Throwable {

        SeleniumControl ctx = SeleniumControl.builder().baseUrl(basePath).driver(() -> webDriver).build();
        AtomicReference<String> path = new AtomicReference<>();

        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {

                path.set(SeleniumContext.resolve(relPath));
            }
        };
        //act
        ctx.apply(stmt, description).evaluate();

        //assert
        assertEquals(expected, path.get());
    }

    @Test
    public void testGetTimeoutProvider_defaultProvider() throws Exception {

        //prepare
        SeleniumContext ctx = new SeleniumContext(() -> webDriver);

        //act
        TimeoutProvider provider = ctx.getTimeoutProvider();

        //assert
        assertNotNull(provider);
        Assert.assertEquals(TimeoutProvider.DEFAULT_PROVIDER, provider);
    }


    @Test
    public void testGetTimeoutProvider_setProvider() throws Exception {

        //prepare
        SeleniumContext ctx = new SeleniumContext(() -> webDriver);
        TimeoutProvider custom = s -> Optional.of(Duration.ofMillis(1234));

        //act
        ctx.setTimeoutProvider(custom);
        TimeoutProvider provider = ctx.getTimeoutProvider();

        //assert
        assertNotNull(provider);
        Assert.assertEquals(custom, provider);
    }
}

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
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;

@RunWith(MockitoJUnitRunner.class)
public class TimeoutsTest {

    @Mock
    private Locator locator;

    @Mock
    private WebDriver webDriver;

    @Mock
    private TimeoutProvider provider;
    private SeleniumContext ctx;

    @Before
    public void setUp() throws Exception {
        when(locator.timeoutKey()).thenReturn("");
        when(locator.timeout()).thenReturn(60);
        this.ctx = new SeleniumContext(() -> webDriver);
        this.ctx.setTimeoutProvider(provider);

    }

    @After
    public void tearDown() throws Exception {
        SeleniumContext.currentContext().ifPresent(SeleniumContext::destroy);
    }

    @Test
    public void testGetOptionalTimeout_locator_locatorTime() throws Exception {
        //prepare

        //act
        Optional<Duration> timeout = Timeouts.getOptionalTimeout(locator);

        //assert
        assertNotNull(timeout);
        assertTrue(timeout.isPresent());
        assertEquals(Long.valueOf(60), timeout.map(Duration::getSeconds).get());
    }

    @Test
    public void testGetOptionalTimeout_locator_providerTime() throws Exception {
        //prepare
        ctx.init();
        when(locator.timeoutKey()).thenReturn("myKey");
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Optional<Duration> timeout = Timeouts.getOptionalTimeout(locator);

        //assert
        assertNotNull(timeout);
        assertTrue(timeout.isPresent());
        assertEquals(Long.valueOf(123), timeout.map(Duration::getSeconds).get());
    }

    @Test
    public void testGetOptionalTimeout_locator_noProviderTime_empty() throws Exception {
        //prepare
        ctx.init();
        when(locator.timeoutKey()).thenReturn("missingKey");
        when(provider.getTimeoutFor("missingKey")).thenReturn(Optional.empty());

        //act
        Optional<Duration> timeout = Timeouts.getOptionalTimeout(locator);

        //assert
        assertNotNull(timeout);
        assertFalse(timeout.isPresent());
    }

    @Test
    public void testGetOptionalTimeout_key() throws Exception {
        //prepare
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Optional<Duration> timeout = Timeouts.getOptionalTimeout("myKey");

        //assert
        assertNotNull(timeout);
        assertTrue(timeout.isPresent());
        assertEquals(Long.valueOf(123), timeout.map(Duration::getSeconds).get());
    }

    @Test
    public void testGetOptionalTimeout_missingKey() throws Exception {
        //prepare
        ctx.init();
        when(provider.getTimeoutFor("missingKey")).thenReturn(Optional.empty());

        //act
        Optional<Duration> timeout = Timeouts.getOptionalTimeout("missingKey");

        //assert
        assertNotNull(timeout);
        assertFalse(timeout.isPresent());
    }

    @Test
    public void testGetTimeout_locator() throws Exception {
        //prepare

        //act
        Duration timeout = Timeouts.getTimeout(locator);


        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(60), timeout);
    }

    @Test
    public void testGetTimeout_locator_byKey() throws Exception {
        //prepare
        ctx.init();
        when(locator.timeoutKey()).thenReturn("myKey");
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Duration timeout = Timeouts.getTimeout(locator);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(123), timeout);
    }

    @Test
    public void testGetTimeout_locator_byMissingKey() throws Exception {
        //prepare
        ctx.init();
        when(locator.timeoutKey()).thenReturn("myKey");
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.empty());

        //act
        Duration timeout = Timeouts.getTimeout(locator);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(60), timeout);
    }

    @Test
    public void testGetTimeout_byKey() throws Exception {

        //prepare
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Duration timeout = Timeouts.getTimeout("myKey");

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(123), timeout);
    }

    @Test
    public void testGetTimeout_byKey_noValue_default() throws Exception {

        //prepare
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.empty());

        //act
        Duration timeout = Timeouts.getTimeout("myKey");

        //assert
        assertNotNull(timeout);
        assertEquals(TimeoutProvider.DEFAULT_TIMEOUT, timeout);
    }

    @Test
    public void testGetTimeout_byKey_noKey_Default() throws Exception {

        //prepare

        //act
        Duration timeout = Timeouts.getTimeout("myKey");

        //assert
        assertNotNull(timeout);
        assertEquals(TimeoutProvider.DEFAULT_TIMEOUT, timeout);
    }


    @Test
    public void testGetTimeout_optionalTimeout_fallbackKeyIgnored() throws Exception {
        //prepare
        Optional<Locator> loc = Optional.of(locator);

        //act
        Duration timeout = Timeouts.getTimeout(loc, "fallbackKey");

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(60), timeout);
    }

    @Test
    public void testGetTimeout_optionalTimeout_fallbackTimeoutIgnored() throws Exception {
        //prepare
        Optional<Locator> loc = Optional.of(locator);

        //act
        Duration timeout = Timeouts.getTimeout(loc, 123);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(60), timeout);
    }

    @Test
    public void testGetTimeout_optionalTimeout_fallbacksIgnored() throws Exception {
        //prepare
        Optional<Locator> loc = Optional.of(locator);

        //act
        Duration timeout = Timeouts.getTimeout(loc, "ignoredKey", 123);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(60), timeout);
    }

    @Test
    public void testGetTimeout_emptyTimeout_fallbackTimeout() throws Exception {
        //prepare

        //act
        Duration timeout = Timeouts.getTimeout(Optional.empty(), 123);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofMillis(123), timeout);
    }

    @Test
    public void testGetTimeout_emptyTimeout_fallbackKey() throws Exception {
        //prepare
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Duration timeout = Timeouts.getTimeout(Optional.empty(), "myKey");

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(123), timeout);
    }

    @Test
    public void testGetTimeout_emptyTimeout_fallbackKey_noContext_default() throws Exception {
        //prepare

        //act
        Duration timeout = Timeouts.getTimeout(Optional.empty(), "myKey");

        //assert
        assertNotNull(timeout);
        assertEquals(TimeoutProvider.DEFAULT_TIMEOUT, timeout);
    }

    @Test
    public void testGetTimeout_emptyTimeout_fallbackKey_missingKey_default() throws Exception {
        //prepare
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.empty());

        //act
        Duration timeout = Timeouts.getTimeout(Optional.empty(), "myKey");

        //assert
        assertNotNull(timeout);
        assertEquals(TimeoutProvider.DEFAULT_TIMEOUT, timeout);
    }

    @Test
    public void testGetTimeout_optionalTimeout_locatorKeyMissing_fallbackTimeIgnored() throws Exception {
        ctx.init();
        when(locator.timeoutKey()).thenReturn("locatorKey");
        when(provider.getTimeoutFor("locatorKey")).thenReturn(Optional.empty());
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Duration timeout = Timeouts.getTimeout(Optional.of(locator), "myKey", 456);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(123), timeout);
    }

    @Test
    public void testGetTimeout_emptyTimeout_fallbackKey_fallbackTimeIgnored() throws Exception {
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.of(Duration.ofSeconds(123)));

        //act
        Duration timeout = Timeouts.getTimeout(Optional.empty(), "myKey", 456);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofSeconds(123), timeout);
    }

    @Test
    public void testGetTimeout_emptyTimeout_missingFallbackKey_fallbackTime() throws Exception {
        ctx.init();
        when(provider.getTimeoutFor("myKey")).thenReturn(Optional.empty());

        //act
        Duration timeout = Timeouts.getTimeout(Optional.empty(), "myKey", 456);

        //assert
        assertNotNull(timeout);
        assertEquals(Duration.ofMillis(456), timeout);
    }
}

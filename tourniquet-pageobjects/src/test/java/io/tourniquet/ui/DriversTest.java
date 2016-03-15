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

package io.tourniquet.ui;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;

/**
 *
 */
public class DriversTest {

    @Test
    @Ignore
    public void testFirefoxDriver() throws Exception {
        assertTrue(Drivers.FIREFOX.get() instanceof FirefoxDriver);
    }

    @Test
    @Ignore
    public void testIEDriver() throws Exception {
        assumeTrue(System.getProperties().containsKey("webdriver.ie.driver"));
        assertTrue(Drivers.IEXPLORER.get() instanceof InternetExplorerDriver);
    }

    @Test
    @Ignore
    public void testChromeDriver() throws Exception {
        assumeTrue(System.getProperties().containsKey("webdriver.ie.driver"));
        assertTrue(Drivers.CHROME.get() instanceof ChromeDriver);
    }

    @Test
    @Ignore
    public void testSafariDriver() throws Exception {
        assumeTrue(System.getenv("Path").contains("Safari"));
        assertTrue(Drivers.SAFARI.get() instanceof SafariDriver);
    }

    @Test
    public void testHeadlessDriver() throws Exception {
        assertTrue(Drivers.HEADLESS.get() instanceof HtmlUnitDriver);
    }

}
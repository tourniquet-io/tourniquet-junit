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

package io.tourniquet.selenium;

import static io.tourniquet.junit.net.NetworkUtils.findAvailablePort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import io.tourniquet.junit.http.rules.HttpMethod;
import io.tourniquet.junit.http.rules.HttpServer;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

/**
 * Example for using the SeleniumControl rule to initialize the WebDriver and access it from the shared SeleniumContext.
 */
public class SeleniumControlExample {

    private static int port = findAvailablePort();

    //use this rule to simulate an http endpoint
    @ClassRule
    public static HttpServer http = new HttpServer("localhost", port);

    //setup the selenium context
    @Rule
    public SeleniumControl control = SeleniumControl.builder()
                                                    .driver(Drivers.HEADLESS)
                                                    .baseUrl("http://localhost:" + port + "/home")
                                                    .build();

    //prepare some synthetic resources so the driver can receive actual content
    @BeforeClass
    public static void setUp() throws Exception {
        http.on(HttpMethod.GET).resource("/home/page1").respond("PAGE1");
        http.on(HttpMethod.GET).resource("/home").respond("HOME");
    }

    //check if the baseURL is loaded initially
    @Test
    public void accessBaseUrl() throws Exception {

        WebDriver driver = SeleniumContext.currentDriver();

        assertNotNull(driver);
        assertEquals("HOME", driver.getPageSource());
    }

    //check if relative resource are correctly resolved
    @Test
    public void getPage() throws Exception {

        WebDriver driver = SeleniumContext.currentDriver();

        driver.get(SeleniumContext.resolve("page1"));
        assertEquals("PAGE1", driver.getPageSource());

    }
}

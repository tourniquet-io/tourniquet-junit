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

package io.tourniquet.junit.http.rules.examples;

import static io.tourniquet.junit.http.rules.HttpMethod.GET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import org.junit.Rule;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import io.tourniquet.junit.http.rules.HttpServer;
import io.tourniquet.junit.http.rules.HttpServerBuilder;

public class HttpServerContentFromUrlQueryExample {

    private final URL resource = HttpServerContentFromUrlQueryExample.class.getResource("index.html");

    @Rule
    public HttpServer server = new HttpServerBuilder().contentFrom("/index.html?param=value", resource).build();

    @Test
    public void testHttpServerGet_notMatching_empty() throws Exception {
        //prepare

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html?other=value");

            //assert
            assertEquals("", page.getContent());
        }
    }

    @Test
    public void testHttpServerGet_matchingQuery() throws Exception {
        //prepare

        //act
        try (final WebClient webClient = new WebClient()) {

            final HtmlPage page = webClient.getPage(server.getBaseUrl() + "/index.html?param=value");
            final String pageAsXml = page.asXml();
            final String pageAsText = page.asText();

            //assert
            assertEquals("Test Content", page.getTitleText());
            assertTrue(pageAsXml.contains("<body>"));
            assertTrue(pageAsText.contains("Test Content Body"));
        }
    }

    @Test
    public void testHttpServerGet_matchingQuery_duplicateParams() throws Exception {
        //prepare

        server.on(GET).resource("/index.html?param=value1&param=value2").respond(resource);

        //act
        try (final WebClient webClient = new WebClient()) {

            final HtmlPage page = webClient.getPage(server.getBaseUrl() + "/index.html?param=value1&param=value2");
            final String pageAsXml = page.asXml();
            final String pageAsText = page.asText();

            //assert
            assertEquals("Test Content", page.getTitleText());
            assertTrue(pageAsXml.contains("<body>"));
            assertTrue(pageAsText.contains("Test Content Body"));
        }
    }

}

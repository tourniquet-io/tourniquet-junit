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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import io.tourniquet.junit.http.rules.HttpServer;
import io.tourniquet.junit.http.rules.HttpServerBuilder;
import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.rules.builder.TemporaryFileBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TemporaryFolder;

/**
 * Created by Gerald Muecke on 04.12.2015.
 */
public class HttpServerContentFromTemporaryFileExample {

    public TemporaryFolder folder = new TemporaryFolder();
    public TemporaryFile file = new TemporaryFileBuilder(folder, "index.html").fromClasspathResource("index.html").build();

    public HttpServer server = new HttpServerBuilder().contentFrom("/index.html", file).build();
    @Rule
    public RuleChain rule = RuleChain.outerRule(folder).around(file).around(server);

    @Test
    public void testHttpServerGet() throws Exception {
        //prepare

        //act
        try (final WebClient webClient = new WebClient()) {

            final HtmlPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsXml = page.asXml();
            final String pageAsText = page.asText();

            //assert
            assertEquals("Test Content", page.getTitleText());
            assertTrue(pageAsXml.contains("<body>"));
            assertTrue(pageAsText.contains("Test Content Body"));
        }
    }
}

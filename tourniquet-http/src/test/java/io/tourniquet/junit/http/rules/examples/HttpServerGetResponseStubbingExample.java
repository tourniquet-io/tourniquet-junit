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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import io.tourniquet.junit.http.rules.HttpServer;
import io.tourniquet.junit.http.rules.HttpServerBuilder;
import org.junit.Rule;
import org.junit.Test;

public class HttpServerGetResponseStubbingExample {


    @Rule
    public HttpServer server = new HttpServerBuilder().port(55555).build();

    @Test
    public void testHttpServerGet() throws Exception {
        //prepare
        server.on(GET).resource("/index.html").respond("someContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsText = page.getContent();

            //assert
            assertEquals("someContent", pageAsText);
        }

    }

    @Test
    public void testHttpServerGetWithQuery() throws Exception {
        //prepare
        server.on(GET).resource("/index.html?param=value").respond("otherContent");
        server.on(GET).resource("/index.html").respond("someContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page1 = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String page1AsText = page1.getContent();

            final TextPage page2 = webClient.getPage(server.getBaseUrl() + "/index.html?param=value");
            final String page2AsText = page2.getContent();

            //assert
            assertEquals("someContent", page1AsText);
            assertEquals("otherContent", page2AsText);
        }

    }

    @Test
    public void testHttpServerGetWithQuery_notMatching() throws Exception {
        //prepare
        server.on(GET).resource("/index.html?param=value").respond("otherContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html?other=value");
            final String pageAsText = page.getContent();

            //assert
            assertEquals("", pageAsText);
        }

    }
}

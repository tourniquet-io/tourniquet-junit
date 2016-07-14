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

import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;

import io.tourniquet.junit.http.rules.HttpServer;
import io.tourniquet.junit.http.rules.HttpServerBuilder;

public class HttpServerPostResponseStubbingExample {


    @Rule
    public HttpServer server = new HttpServerBuilder().port(55555).build();

    @Test
    public void testHttpServerGet_noParams() throws Exception {
        //prepare
        server.onPost("/index.html").respond("someContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsText = page.getContent();

            //assert
            assertEquals("someContent", pageAsText);
        }

    }

    @Test
    public void testHttpServerGet_withParams() throws Exception {
        //prepare
        final Map<String, String> params = new HashMap<>();
        params.put("field1", "value1");
        params.put("field2", "value2");
        server.onPost("/index.html").withParams(params).respond("someContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsText = page.getContent();

            //assert
            assertEquals("someContent", pageAsText);
        }

    }

    @Test
    public void testHttpServerGet_withPayload() throws Exception {
        //prepare
        byte[] data = "Test Content".getBytes();
        server.onPost("/index.html").withPayload(data).respond("someContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsText = page.getContent();

            //assert
            assertEquals("someContent", pageAsText);
        }

    }

    @Test
    public void testHttpServerGet_reactOnRequest() throws Exception {
        //prepare
        final Map<String, String> params = new HashMap<>();
        params.put("field1", "value1");
        params.put("field2", "value2");
        server.onPost("/index.html").withParams(params).execute(x -> {}).respond("someContent");

        //act
        try (final WebClient webClient = new WebClient()) {

            final TextPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsText = page.getContent();

            //assert
            assertEquals("someContent", pageAsText);
        }

    }

}

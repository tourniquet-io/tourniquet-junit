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

import static io.tourniquet.junit.http.rules.HttpMethod.POST;
import static io.tourniquet.junit.http.rules.examples.HttpClientHelper.getString;
import static io.tourniquet.junit.http.rules.examples.HttpClientHelper.param;
import static io.tourniquet.junit.http.rules.examples.HttpClientHelper.post;
import static org.junit.Assert.assertEquals;

import java.io.IOException;

import io.tourniquet.junit.http.rules.HttpServer;
import io.tourniquet.junit.http.rules.HttpServerBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Rule;
import org.junit.Test;

public class HttpServerPostResponseStubbingExample {

    @Rule
    public HttpServer server = new HttpServerBuilder().port(55555).build();

    @Test
    public void testHttpServerPost_noParams() throws Exception {
        //prepare
        server.on(POST).resource("/action.do").respond("someContent");

        //act
        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do"))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }
    }

    @Test
    public void testHttpServerPost_withParams() throws Exception {
        //prepare
        server.on(POST)
              .resource("/action.do")
              .withParam("field1", "value1")
              .withParam("field2", "value2")
              .respond("someContent");

        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do",
                                                                  param("field1", "value1"),
                                                                  param("field2", "value2")))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }

    }

    @Test
    public void testHttpServerPost_withPayload() throws Exception {
        //prepare
        byte[] data = "Test Content".getBytes();
        server.on(POST).resource("/action.do").withPayload(data).respond("someContent");

        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do", data))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }
    }

    @Test
    public void testHttpServerPost_reactOnRequest() throws Exception {
        //prepare
        server.on(POST).resource("/action.do").execute(x -> {
            try {
                x.getOutputStream().write("someContent".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do"))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }
    }

    //=========== helper methods to create requests =================

}

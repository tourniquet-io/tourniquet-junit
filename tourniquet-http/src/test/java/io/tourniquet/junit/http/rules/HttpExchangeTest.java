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

package io.tourniquet.junit.http.rules;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Deque;
import java.util.Map;

import io.undertow.UndertowOptions;
import io.undertow.server.BlockingHttpExchange;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.Cookie;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.xnio.OptionMap;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpExchangeTest {

    @Mock
    private BlockingHttpExchange blockingHttpExchange;

    @Rule
    public HttpServerExchangeMock xchg = new HttpServerExchangeMock();

    /**
     * The class under test
     */
    private HttpExchange subject;
    private HttpServerExchange exchange;

    @Before
    public void setUp() throws Exception {

        exchange = xchg.getExchange();
        subject = new HttpExchange(xchg.getExchange());
    }

    @Test
    public void testGetRequestMethod() throws Exception {
        //prepare
        exchange.setRequestMethod(HttpString.tryFromString("POST"));

        //act
        String method = subject.getRequestMethod();

        //assert
        assertEquals("POST", method);
    }

    @Test
    public void testGetHost() throws Exception {
        //prepare
        exchange.setDestinationAddress(InetSocketAddress.createUnresolved("somehost", 12345));

        //act
        String host = subject.getHost();

        //assert
        assertEquals("somehost", host);
    }

    @Test
    public void testGetPort() throws Exception {
        //prepare
        exchange.setDestinationAddress(InetSocketAddress.createUnresolved("somehost", 12345));

        //act
        int port = subject.getPort();

        //assert
        assertEquals(12345, port);
    }

    @Test
    public void testGetHostAndPort() throws Exception {
        //prepare
        exchange.setRequestScheme("http");
        exchange.setDestinationAddress(InetSocketAddress.createUnresolved("somehost", 12345));

        //act
        String hostAndPort = subject.getHostAndPort();

        //assert
        assertEquals("somehost:12345", hostAndPort);
    }

    @Test
    public void testGetInputStream() throws Exception {
        //prepare
        when(blockingHttpExchange.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
        exchange.startBlocking(blockingHttpExchange);

        //act
        InputStream is = subject.getInputStream();

        //assert
        assertNotNull(is);
        assertEquals("content", IOUtils.toString(is));
    }

    @Test
    public void testGetInputStream_reRead() throws Exception {
        //prepare
        when(blockingHttpExchange.getInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));
        exchange.startBlocking(blockingHttpExchange);

        //act
        //first read
        assertEquals("content", IOUtils.toString(subject.getInputStream()));
        //second read
        assertEquals("content", IOUtils.toString(subject.getInputStream()));
    }

    @Test
    public void testGetPayload() throws Exception {
        //prepare
        byte[] data = "content".getBytes();
        when(blockingHttpExchange.getInputStream()).thenReturn(new ByteArrayInputStream(data));
        exchange.startBlocking(blockingHttpExchange);

        //act
        byte[] payload = subject.getPayload();

        //assert
        assertArrayEquals(data, payload);
    }

    @Test
    public void testGetOutputStream() throws Exception {
        //prepare
        exchange.startBlocking();

        //act
        OutputStream os = subject.getOutputStream();
        os.write("test".getBytes());

        //assert
        assertNotNull(os);
        xchg.getBuffer().rewind();
        byte[] data = new byte[4];
        xchg.getBuffer().get(data);
        assertEquals("test", new String(data));
    }

    @Test
    public void testGetRequestScheme() throws Exception {
        //prepare
        exchange.setRequestScheme("http");

        //act
        String scheme = subject.getRequestScheme();

        //assert
        assertEquals("http", scheme);

    }

    @Test
    public void testGetRequestPath() throws Exception {
        //prepare
        exchange.setRequestPath("testpath");

        //act
        String path = subject.getRequestPath();

        //assert
        assertEquals("testpath", path);
    }

    @Test
    public void testGetRelativePath() throws Exception {
        //prepare
        exchange.setRelativePath("testpath");

        //act
        String path = subject.getRelativePath();

        //assert
        assertEquals("testpath", path);
    }

    @Test
    public void testGetResolvedPath() throws Exception {
        //prepare
        exchange.setResolvedPath("testpath");

        //act
        String path = subject.getResolvedPath();

        //assert
        assertEquals("testpath", path);
    }

    @Test
    public void testGetQueryString() throws Exception {
        //prepare
        exchange.setQueryString("querystring");

        //act
        String query = subject.getQueryString();

        //assert
        assertEquals("querystring", query);
    }

    @Test
    public void testGetRequestURI() throws Exception {
        //prepare
        exchange.setRequestURI("requestUri");

        //act
        String uri = subject.getRequestURI();

        //assert
        assertEquals("requestUri", uri);
    }

    @Test
    public void testGetRequestURL() throws Exception {
        //prepare
        exchange.setRequestScheme("http");
        exchange.setDestinationAddress(InetSocketAddress.createUnresolved("somehost", 12345));
        exchange.setRequestURI("/requestUri");

        //act
        String uri = subject.getRequestURL();

        //assert
        assertEquals("http://somehost:12345/requestUri", uri);
    }

    @Test
    public void testGetPathParameters() throws Exception {
        //prepare
        exchange.addPathParam("param", "value");

        //act
        Map<String, Deque<String>> params = subject.getPathParameters();

        //assert
        assertNotNull(params);
        assertTrue(params.containsKey("param"));
        assertTrue(params.get("param").contains("value"));
    }

    @Test
    public void testGetQueryParameters() throws Exception {
        //prepare
        exchange.addQueryParam("param", "value");

        //act
        Map<String, Deque<String>> params = subject.getQueryParameters();

        //assert
        assertNotNull(params);
        assertTrue(params.containsKey("param"));
        assertTrue(params.get("param").contains("value"));
    }

    @Test
    public void testGetRequestHeaders() throws Exception {
        //prepare
        HeaderMap headerMap = exchange.getRequestHeaders();
        headerMap.add(Headers.ACCEPT, "text/plain");

        //act
        Map<String, Deque<String>> headers = subject.getRequestHeaders();

        //assert
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertTrue(headers.get("Accept").contains("text/plain"));
    }

    @Test
    public void testGetRequestCookies() throws Exception {
        //prepare
        ServerConnection scon = xchg.getServerConnection();
        OptionMap map = OptionMap.builder()
                                 .set(UndertowOptions.MAX_COOKIES, 100)
                                 .set(UndertowOptions.ALLOW_EQUALS_IN_COOKIE_VALUE, false)
                                 .getMap();
        when(scon.getUndertowOptions()).thenReturn(map);

        HeaderMap headerMap = exchange.getRequestHeaders();
        headerMap.add(Headers.COOKIE, "cookie=name");

        //act
        Map<String, String> cookies = subject.getRequestCookies();

        //assert
        assertNotNull(cookies);
        assertTrue(cookies.containsKey("cookie"));
        assertEquals("name", cookies.get("cookie"));
    }

    @Test
    public void testAddResponseCookie() throws Exception {
        //prepare

        //act
        subject.addResponseCookie("cookie", "name");

        //assert
        Map<String, Cookie> cookies = exchange.getResponseCookies();
        assertTrue(cookies.containsKey("cookie"));
        assertEquals("cookie", cookies.get("cookie").getName());
        assertEquals("name", cookies.get("cookie").getValue());
    }

    @Test
    public void testGetContentLength() throws Exception {
        //prepare
        HeaderMap headerMap = exchange.getRequestHeaders();
        headerMap.add(Headers.CONTENT_LENGTH, 123456L);

        //act
        long length = subject.getContentLength();

        //assert
        assertEquals(123456L, length);
    }

    @Test
    public void testGetSourceAddress() throws Exception {
        //prepare
        exchange.setSourceAddress(InetSocketAddress.createUnresolved("somehost", 12345));

        //act
        InetSocketAddress source = subject.getSourceAddress();

        //assert
        assertNotNull(source);
        assertEquals("somehost", source.getHostName());
        assertEquals(12345, source.getPort());
    }

    @Test
    public void testGetDestinationAddress() throws Exception {
        //prepare
        exchange.setDestinationAddress(InetSocketAddress.createUnresolved("somehost", 12345));

        //act
        InetSocketAddress dest = subject.getDestinationAddress();

        //assert
        assertNotNull(dest);
        assertEquals("somehost", dest.getHostName());
        assertEquals(12345, dest.getPort());
    }

    @Test
    public void testSetResponseContentLength() throws Exception {
        //prepare

        //act
        subject.setResponseContentLength(123456L);

        //assert
        long contentLength = exchange.getResponseContentLength();
        assertEquals(123456L, contentLength);
    }

    @Test
    public void testSetStatusCode() throws Exception {
        //prepare

        //act
        subject.setStatusCode(404);

        //assert
        assertEquals(404, exchange.getStatusCode());
    }

    @Test
    public void testAddResponseHeader() throws Exception {
        //prepare

        //act
        subject.addResponseHeader("Content-Type", "text/plain");

        //assert
        HeaderMap responseHeaders = exchange.getResponseHeaders();
        assertTrue(responseHeaders.contains("Content-Type"));
        assertEquals("text/plain", responseHeaders.get("Content-Type").element());
    }
}

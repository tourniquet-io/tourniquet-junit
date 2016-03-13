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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.net.URL;

import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.net.NetworkMatchers;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 07.12.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpServerBuilderTest {

    /**
     * The class under test
     */
    @InjectMocks
    private HttpServerBuilder subject;

    @Test
    public void testBuild_defaultValues() throws Exception {
        //prepare

        //act
        HttpServer server = subject.build();

        //assert
        assertNotNull(server);
        assertEquals("localhost", server.getHostname());
        assertTrue(server.getPort() > 1024);
        assertThat(NetworkMatchers.port(server.getPort()), NetworkMatchers.isAvailable());
    }

    @Test
    public void testBuild_onlyCustomPort() throws Exception {
        //prepare

        //act
        HttpServer server = subject.port(55555).build();

        //assert
        assertNotNull(server);
        assertEquals("localhost", server.getHostname());
        assertEquals(55555, server.getPort());
    }

    @Test
    public void testBuild_onlyCustomHost() throws Exception {
        //prepare

        //act
        HttpServer server = subject.hostname("someHost").build();

        //assert
        assertNotNull(server);
        assertEquals("someHost", server.getHostname());
        assertTrue(server.getPort() > 1024);
        assertThat(NetworkMatchers.port(server.getPort()), NetworkMatchers.isAvailable());
    }

    @Test
    public void testBuild_withPort_and_Hostname() throws Exception {
        //prepare

        //act
        HttpServer server = subject.port(55555).hostname("somehost").build();

        //assert
        assertNotNull(server);
        assertEquals("somehost", server.getHostname());
        assertEquals(55555, server.getPort());
    }

    @Test
    public void testContentFrom_resourceExists() throws Exception {
        //prepare

        //act
        HttpServerBuilder builder = subject.contentFrom("/", "HttpServerBuilderTest_testZipContent.zip");

        //assert
        assertSame(subject, builder);

    }

    @Test(expected = AssertionError.class)
    public void testContentFrom_resourceNotExists_fail() throws Exception {
        //prepare

        //act
        HttpServerBuilder builder = subject.contentFrom("/", "nonExisting");

        //assert
        assertSame(subject, builder);

    }

    @Test
    public void testContentFrom_temporaryFile() throws Exception {
        //prepare
        TemporaryFile file = mock(TemporaryFile.class);

        //act
        HttpServerBuilder builder = subject.contentFrom("/", file);

        //assert
        assertSame(subject, builder);

    }
    @Test
    public void testContentFrom_temporaryFolder() throws Exception {
        //prepare
        TemporaryFolder folder = mock(TemporaryFolder.class);

        //act
        HttpServerBuilder builder = subject.contentFrom("/", folder);

        //assert
        assertSame(subject, builder);

    }

    @Test
    public void testContentFrom_url() throws Exception {
        //prepare
        URL url = new URL("file:///");

        //act
        HttpServerBuilder builder = subject.contentFrom("/", url);

        //assert
        assertSame(subject, builder);

    }
}

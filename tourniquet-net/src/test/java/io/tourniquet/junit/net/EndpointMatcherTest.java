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

package io.tourniquet.junit.net;

import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EndpointMatcherTest {

    /**
     * The class under test
     */
    @InjectMocks
    private EndpointMatcher subject;

    @Mock
    private NetworkPort networkPort;

    @Mock
    private Description description;

    private ServerSocket server;
    private int port;

    @Before
    public void setUp() throws Exception {

        port = NetworkUtils.findAvailablePort();
        server = new ServerSocket(port);
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    server.accept();
                } catch (IOException e) {
                    //ignore
                }
            }
        }).start();
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWithin_negativeTimout() throws Exception {

        //prepare
        when(networkPort.getSocketAddress()).thenReturn(new InetSocketAddress(port));

        //act
        subject.within(-10, TimeUnit.SECONDS);

        //assert
        subject.matches(networkPort);
    }

    @Test
    public void testMatches_nonTcpPort_false() throws Exception {

        //prepare
        Object item = new Object();

        //act
        boolean actual = subject.matches(item);

        //assert
        assertFalse(actual);
    }

    @Test
    public void testMatches_tcpPort_true() throws Exception {

        //prepare

        when(networkPort.getSocketAddress()).thenReturn(new InetSocketAddress(port));

        //act
        boolean actual = subject.matches(networkPort);

        //assert
        assertTrue(actual);
    }

    @Test
    public void testMatches_tcpPortUnreachable_false() throws Exception {

        //prepare
        when(networkPort.getSocketAddress()).thenReturn(new InetSocketAddress(port + 1));

        //act
        boolean actual = subject.matches(networkPort);

        //assert
        assertFalse(actual);
    }

    @Test
    public void testDescribeTo() throws Exception {

        //prepare

        //act
        subject.describeTo(description);

        //assert
        verify(description).appendText("Port reachable");
    }
}

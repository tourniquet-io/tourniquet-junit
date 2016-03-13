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

import static io.tourniquet.junit.net.NetworkUtils.findAvailablePort;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.AssumptionViolatedException;

public class NetworkUtilsTest {

    private int defaultOffset;

    @Before
    public void setUp() throws Exception {
        defaultOffset = NetworkUtils.PORT_OFFSET.get();
    }

    @After
    public void tearDown() throws Exception {
        NetworkUtils.PORT_OFFSET.set(defaultOffset);
    }

    @Test
    public void testFindAvailablePort_defaultRetries_portAvailable_ok() throws Exception {

        //prepare

        //act
        int port = NetworkUtils.findAvailablePort();

        //assert
        assertTrue("Port " + port + " is not available", portAvailable(port));
    }

    private boolean portAvailable(final int port) throws IOException {

        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Test
    public void testFindAvailablePort_defaultRetries_noPortAvailable_ignore() throws Exception {
        //prepare
        NetworkUtils.PORT_OFFSET.set(65536 - 1024 - 1);
        try (ServerSocket socket = new ServerSocket(65535)) {
            //act
            int port = NetworkUtils.findAvailablePort();
            fail("AssumptionViolatedException expected");
        } catch (AssumptionViolatedException e) {
            //have to catch the exception to prove its working, otherwise the test will be ignored
        }

    }

    @Test
    public void testFindAvailablePort_oneTry_portAvailable_ok() throws Exception {

        //prepare

        //act
        int port = NetworkUtils.findAvailablePort(1);

        //assert
        assertTrue("Port " + port + " is not available", portAvailable(port));
    }

    @Test
    public void testFindAvailablePort_oneTry_noPortAvailable_ignore() throws Exception {

        NetworkUtils.PORT_OFFSET.set(65536 - 1024 - 1);
        try (ServerSocket socket = new ServerSocket(65535)) {
            //act
            //specifying 1 is semantically equal to 0. There is at minimum one try to get an available port.
            int port = NetworkUtils.findAvailablePort(1);
            fail("AssumptionViolatedException expected");
        } catch (AssumptionViolatedException e) {
            //have to catch the exception to prove its working, otherwise the test will be ignored
        }
    }

    @Test
    public void testFindAvailablePort_maxRetries_portAvailable_ok() throws Exception {

        //prepare

        //act
        int port = NetworkUtils.findAvailablePort(10);

        //assert
        assertTrue("Port " + port + " is not available", portAvailable(port));
    }

    @Test
    public void testFindAvailablePort_maxRetries_retryLimitReached_portAvailable() throws Exception {

        //prepare

        //act
        //only one try
        int port = NetworkUtils.findAvailablePort(0);

        //assert
        assertTrue("Port " + port + " is not available", portAvailable(port));
    }

    @Test
    public void testFindAvailablePort_maxRetries_retryLimitReached_noPortAvailable_ignore() throws Exception {

        //prepare
        NetworkUtils.PORT_OFFSET.set(65536 - 1024 - 1);
        try(ServerSocket socket = new ServerSocket(65535)){
            //act
            int port = NetworkUtils.findAvailablePort(0);
            fail("AssumptionViolatedException expected");
        } catch (AssumptionViolatedException e) {
            //have to catch the exception to prove its working, otherwise the test will be ignored
        }

    }

    @Test
    public void testIsPortAvailable() throws Exception {

        //prepare
        int port = new Random().nextInt(65535 - 1024) + 1024;
        assumeTrue("Port " + port + " is not available", portAvailable(port));

        //act
        boolean portAvailable = NetworkUtils.isPortAvailable(port);

        //assert
        assertTrue(portAvailable);

    }

    @Test
    public void testIsPortAvailable_portNotAvailable() throws Exception {

        //prepare
        int port = new Random().nextInt(65536 - 1024) + 1024;
        assumeTrue("Port " + port + " is not available", portAvailable(port));
        //block port by using it
        boolean portAvailable;
        try(ServerSocket socket = new ServerSocket(port)) {
            //act
            portAvailable = NetworkUtils.isPortAvailable(port);
        }
        //assert
        assertFalse(portAvailable);


    }

    @Test
    public void testRandomPort_twoPorts() throws Exception {

        int port1 = NetworkUtils.randomPort();
        int port2 = NetworkUtils.randomPort();

        assertTrue(port1 > 1024);
        assertTrue(port2 > 1024);

        assertTrue(port1 < 65536);
        assertTrue(port2 < 65536);

        assertNotEquals(port1, port2);

    }

    @Test
    public void testRandomPort_manyPorts() throws Exception {

        for (int i = 0, len = 65536 * 20; i < len; i++) {
            int port = NetworkUtils.randomPort();
            assertTrue(port >= 1024);
            assertTrue(port < 65536);
        }
    }

    @Test
    public void testGetPortOffset_default() throws Exception {
        //prepare

        //act
        int offset = NetworkUtils.getPortOffset();

        //assert
        assertEquals(1024, offset);

    }

    @Test
    public void testGetPortOffset_customOffset() throws Exception {
        //prepare
        NetworkUtils.PORT_OFFSET.set(10000);

        //act
        int offset = NetworkUtils.getPortOffset();

        //assert
        assertEquals(11024, offset);

    }
}

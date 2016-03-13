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

import static io.tourniquet.junit.net.NetworkMatchers.isAvailable;
import static io.tourniquet.junit.net.NetworkMatchers.isReachable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;


public class NetworkMatchersTest {

    @Test
    public void testIsAvailable() throws Exception {
        //prepare

        //act
        ResourceAvailabilityMatcher matcher = NetworkMatchers.isAvailable();

        //assert
        assertNotNull(matcher);
    }

    @Test
    public void testIsReachable() throws Exception {
        //prepare

        //act
        EndpointMatcher matcher = NetworkMatchers.isReachable();

        //assert
        assertNotNull(matcher);

    }

    @Test
    public void testPort() throws Exception {

        //prepare

        int expected = 123;

        //act
        NetworkPort port = NetworkMatchers.port(expected);

        //assert
        assertNotNull(port);
        assertEquals(expected, port.getPortNumber());
        assertEquals(NetworkPort.Type.TCP, port.getType());
    }

    @Test
    public void testDatagramPort() throws Exception {

        //prepare

        int expected = 123;

        //act
        NetworkPort port = NetworkMatchers.datagramPort(expected);

        //assert
        assertNotNull(port);
        assertEquals(expected, port.getPortNumber());
        assertEquals(NetworkPort.Type.UDP, port.getType());
    }

    @Test
    public void testRemotePort() throws Exception {
        //prepare
        String host = "localhost";
        int expectedPort = 123;

        //act
        NetworkPort port = NetworkMatchers.remotePort(host, expectedPort);

        //assert
        assertNotNull(port);
        assertEquals(expectedPort, port.getPortNumber());
        assertEquals(host + "/127.0.0.1:" + expectedPort, port.getSocketAddress().toString());
        assertEquals(NetworkPort.Type.TCP, port.getType());

    }

    @Test
    public void testRemoteDatagramPort() throws Exception {
        //prepare
        String host = "localhost";
        int expectedPort = 123;

        //act
        NetworkPort port = NetworkMatchers.remoteDatagramPort(host, expectedPort);

        //assert
        assertNotNull(port);
        assertEquals(expectedPort, port.getPortNumber());
        assertEquals(host + "/127.0.0.1:" + expectedPort, port.getSocketAddress().toString());
        assertEquals(NetworkPort.Type.UDP, port.getType());

    }

}

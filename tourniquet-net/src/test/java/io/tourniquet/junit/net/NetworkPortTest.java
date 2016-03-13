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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.junit.Test;

public class NetworkPortTest {

    @Test
    public void testGetPortNumber() throws Exception {

        //prepare
        int expected = 123;

        //act
        NetworkPort port = new NetworkPort(expected, NetworkPort.Type.TCP);


        //assert
        assertEquals(expected,port.getPortNumber() );
    }

    @Test
    public void testGetSocketAddress() throws Exception {
        //prepare
        int portNumber = 123;
        NetworkPort port = new NetworkPort(portNumber,NetworkPort.Type.TCP);

        //act
        SocketAddress addr = port.getSocketAddress();

        //assert
        assertNotNull(addr);
        assumeTrue(addr instanceof InetSocketAddress);
        assertEquals(portNumber, ((InetSocketAddress)addr).getPort());

    }

    @Test
    public void testGetType() throws Exception {
        //prepare
        int portNumber = 123;
        NetworkPort port = new NetworkPort(portNumber,NetworkPort.Type.TCP);

        //act
        NetworkPort.Type type = port.getType();

        //assert
        assertEquals(NetworkPort.Type.TCP, type);

    }

    @Test
    public void testToString_tcp() throws Exception {
        //prepare
        NetworkPort port = new NetworkPort(123,NetworkPort.Type.TCP);

        //act
        String toString = port.toString();

        //assert
        assertEquals("tcp:123", toString);

    }

    @Test
    public void testToString_udp() throws Exception {
        //prepare
        NetworkPort port = new NetworkPort(123,NetworkPort.Type.UDP);

        //act
        String toString = port.toString();

        //assert
        assertEquals("udp:123", toString);

    }
}

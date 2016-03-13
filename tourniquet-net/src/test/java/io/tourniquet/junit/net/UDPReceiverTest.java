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

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.core.IsNot;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class UDPReceiverTest {

    @Mock
    private Description description;

    private UDPReceiver subject;

    @Before
    public void setUp() throws Exception {
        this.subject = new UDPReceiver();
    }

    @Test
    public void testHasMorePackets_noneInQueue_false() throws Exception {
        assertFalse(subject.hasMorePackets());
    }

    @Test
    public void testHasMorePackets_packetReceived_true() throws Throwable {
        //prepare
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sendPacket("Test".getBytes());
            }
        } ;
        subject.apply(stmt, description).evaluate();

        //act
        boolean result = subject.hasMorePackets();

        //assert
        assertTrue(result);
    }

    @Test
    public void testNextPacket() throws Throwable {
        //prepare
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sendPacket("Test1".getBytes());
                sendPacket("Test2".getBytes());
                sendPacket("Test3".getBytes());

            }
        } ;
        subject.apply(stmt, description).evaluate();

        //act
        byte[] packet1 = subject.nextPacket();
        byte[] packet2 = subject.nextPacket();
        byte[] packet3 = subject.nextPacket();

        //assert
        assertEquals("Test1", new String(packet1));
        assertEquals("Test2", new String(packet2));
        assertEquals("Test3", new String(packet3));

    }

    @Test
    public void testPacketCount() throws Throwable {
        //prepare
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sendPacket("Test1".getBytes());
                sendPacket("Test2".getBytes());
                sendPacket("Test3".getBytes());
            }
        } ;
        subject.apply(stmt, description).evaluate();
        //act
        int count = subject.packetCount();
        //assert
        assertEquals(3, count);
    }

    @Test
    public void testSetBufferSize() throws Throwable {
        //prepare
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sendPacket("Test1".getBytes());

            }
        } ;
        //act
        subject.setBufferSize(4);

        //assert
        subject.apply(stmt, description).evaluate();
        assertTrue(subject.hasMorePackets());
        byte[] packet = subject.nextPacket();
        assertEquals(4, packet.length);
    }

    @Test
    public void testSetServerPort() throws Throwable {
        //prepare
        final AtomicInteger actualPort = new AtomicInteger();
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                actualPort.set(subject.getServerPort());
                sendPacket("Test1".getBytes());
                assertThat(NetworkMatchers.datagramPort(actualPort.get()), IsNot.not(NetworkMatchers.isAvailable()));

            }
        } ;
        //act
        int port = NetworkUtils.findAvailablePort();
        subject.setServerPort(port);

        //assert
        subject.apply(stmt, description).evaluate();
        assertEquals(port, actualPort.get());
    }

    @Test
    public void testOnDatagramReceived() throws Throwable {
        //prepare
        Statement stmt = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                sendPacket("Test".getBytes());
            }
        } ;
        //act
        final List<byte[]> packets = new CopyOnWriteArrayList<>();
        subject.onDatagramReceived(new PacketHandler() {

            @Override
            public void process(final byte[] data) {
                packets.add(data);
            }
        });

        //assert
        subject.apply(stmt, description).evaluate();
        assertEquals(1, packets.size());
        assertEquals("Test", new String(packets.get(0)));
    }

    /**
     * Sends a packet to the test rule
     * @param data
     * @throws IOException
     */
    private void sendPacket(final byte[] data) throws IOException {

        final InetAddress address = InetAddress.getLocalHost();
        final DatagramPacket packet = new DatagramPacket(data, data.length, address, subject.getServerPort());
        try(DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.send(packet);
            Thread.sleep(25);
        } catch (InterruptedException e) {
            //omit
        }
    }
}

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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.tourniquet.junit.UncheckedException;
import io.tourniquet.junit.rules.ExternalResource;
import org.slf4j.Logger;

/**
 * A rule that starts a server thread accepting incoming UDP packages. Using the default packet handler, the UDP
 * datagrams received are put into a queue to be accessed using the queue related methods
 * <ul>
 *     <li>{@link #packetCount()}</li>
 *     <li>{@link #hasMorePackets()}</li>
 *     <li>{@link #nextPacket()}</li>
 * </ul>
 * However, the default handler can be overridden with a custom handler, which will effectively render the
 * above mentioned methods useless.
 * <br>
 * Further, the receiver's buffer can be adjusted. The default size is 2048 bytes. If the datagrams to receive are
 * expected to be larger than this, the size should be increased.
 * <br>
 * The port of the receiver can be set to a specific port, otherwise a random available port is picked, which can
 * be retrieved using the {@link #getServerPort()} method.
 *
 */
public class UDPReceiver extends ExternalResource {

    /**
     * Queue of received incoming packets
     */
    private final Deque<byte[]> packets = new ConcurrentLinkedDeque<>();
    /**
     * The UDP port the receiver listens on. The initial value is -1. In that case it will be assigned an available
     * random port during initialization of the rule.
     */
    private int serverPort = -1;
    /**
     * The buffer size used for receiving incoming packets. If a packet is larger that the size of the buffer, the
     * remainder will be silently omitted.
     */
    private int bufferSize = 2048;
    /**
     * The handler that processes the received packets. The default handler will put it in the packet queue. Note that
     * if the handler gets changed, the packet queue related methods won't work
     */
    private Consumer<byte[]> packetHandler = packets::addLast;
    /**
     * Executor service for managing the receiver thread.
     */
    private ExecutorService threadPool;
    /**
     * The processor that is run in a separate thread to receive the incoming packets
     */
    private UDPProcessor processor;

    @Override
    protected void beforeClass() throws Throwable {

        before();
    }

    @Override
    protected void afterClass() {

        after();
    }

    @Override
    protected void before() throws Throwable {

        if (this.serverPort <= 0) {
            this.serverPort = NetworkUtils.findAvailablePort();
        }
        this.threadPool = Executors.newFixedThreadPool(1);
        this.processor = new UDPProcessor(this.serverPort, this.bufferSize, this.packetHandler);
        this.threadPool.submit(this.processor);
        //waiting for the server to come up
        while (!this.processor.running.get()) {
            Thread.sleep(10);
        }

    }

    @Override
    protected void after() {

        this.processor.stop();
        this.threadPool.shutdownNow();
        try {
            threadPool.awaitTermination(5, SECONDS);
        } catch (InterruptedException e) {
            //omit
        }
    }

    /**
     * Checks if there are more packets in the receive queue.
     * @return
     *  true if there are packets available in the receive queue
     */
    public boolean hasMorePackets(){
        return !this.packets.isEmpty();
    }

    /**
     * Returns the next packet from the receive queue
     * @return
     *  the binary data representing the packet
     */
    public byte[] nextPacket(){
        return this.packets.removeFirst();
    }

    /**
     * The number of packets in the queue.
     * @return
     *  the number of packets in the queue.
     */
    public int packetCount(){
        return this.packets.size();
    }

    /**
     * Sets the size of the receive buffer. If the buffer is too small for the received bytes, the remainder of the
     * received packet is silently discarded. This method has to be invoked before the rule is applied, otherwise it
     * won't have  an effect.
     * Default is 2048 bytes.
     * @param bufferSize
     *  the size of the new buffer.
     */
    public void setBufferSize(final int bufferSize) {
        this.bufferSize = bufferSize;
    }

    /**
     * Returns the port of the sever. In case no particular port has been set, an available random port is chosen
     * on application of the rule.
     * @return
     *  the current tcp port
     */
    public int getServerPort() {

        return serverPort;
    }

    /**
     * Sets the server port to a specific port. If no port is selected or this value is less or equal 0, an available
     * random port is used
     * @param serverPort
     */
    public void setServerPort(final int serverPort) {

        assertStateBefore(State.BEFORE_EXECUTED);
        this.serverPort = serverPort;
    }

    /**
     * Overrides the default packet handler. Note that the methods for reading the received packets won't produce
     * sensible results.
     * @param packetHandler
     *  the new packet handler that is invoked when an UDP datagram is received
     */
    public void onDatagramReceived(final Consumer<byte[]> packetHandler) {

        this.packetHandler = packetHandler;
    }

    /**
     * Processor for incoming
     */
    private static class UDPProcessor implements Runnable { //NOSONAR

        private static final Logger LOG = getLogger(UDPProcessor.class);

        private final AtomicBoolean running = new AtomicBoolean(false);

        /**
         * Listen port of the UDP receiver
         */
        private final int port;
        /**
         * The size of the internal buffer to receive packets
         */
        private final int bufferSize;
        /**
         * The handler that processes the incoming packets
         */
        private final Consumer<byte[]> handler;

        /**
         * Creates a new UDPProcessor on the specified port.
         * @param port
         *  the port to listen for UDP packets
         * @param bufferSize
         *  the buffer to read incoming UDP packets. If a packet is larger that the buffer length, the remainder is
         *  discarded silently.
         * @param handler
         *  the handler that is invoked on received packets
         */
        public UDPProcessor(final int port, final int bufferSize, final Consumer<byte[]> handler) {

            this.port = port;
            this.bufferSize = bufferSize;
            this.handler = handler;
        }

        /**
         * Stops the server
         */
        public void stop() {

            running.set(false);
        }

        @Override
        public void run() {
            try (DatagramChannel channel = DatagramChannel.open()){
                channel.socket().bind(new InetSocketAddress(this.port));
                final ByteBuffer buf = ByteBuffer.allocate(this.bufferSize);
                //setting running true after the buffer allocation
                //to support waiting for the server to initialize properly
                running.set(true);
                while (running.get()) {
                    receivePacket(channel, buf);
                }
            } catch (IOException e) {
                throw new UncheckedException("Could not start UDP receiver", e);
            }
            LOG.info("Server stopped");
        }

        /**
         * Waits for an incoming packet. If a packet has been received, its read from the buffer and passed
         * to the packet handler
         * @param channel
         *  the datagram channel to receive incoming UDP packets
         * @param buf
         *  the buffer where the packets are received
         */
        private void receivePacket(final DatagramChannel channel, final ByteBuffer buf) {
            buf.clear();
            try {
                channel.receive(buf);
            } catch (IOException e) {
                LOG.warn("Could not read packet", e);
            }
            buf.flip();
            if(buf.remaining() > 0){
                final byte[] receivedData = new byte[buf.remaining()];
                LOG.debug("Received {} byte packet", receivedData.length);
                buf.get(receivedData);
                handler.accept(receivedData);
            }
        }

    }
}

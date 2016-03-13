Rules
=====

The network module contains some rules to simulate behavior in a network context.


UDPReceiver
------------------------------------

The UDPReceiver may be used to record incoming Datagram packets and may be used to test code that send UDP packets
to an endpoint, i.e. a Syslogger. It simply stores the received data that can be verified after the code has sent
the packets.

Example:

```java
    @Rule
    public UDPReceiver receiver = new UDPReceiver();
    
    ...
    @Test
    public void testUDP() {
        //send UDP datagram
        byte[] data = "Example".getBytes();
        final InetAddress address = InetAddress.getLocalHost();
        final DatagramPacket packet = new DatagramPacket(data, data.length, address, subject.getServerPort());
        try(DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.send(packet);
        } catch (InterruptedException e) {
            //omit
        }
                
        assertTrue(receiver.hasMorePackets());
        assertEquals("Example", new String(receiver.nextPacket()));
    }
    
```

Note that because of the UDPReceiver runs on a separate thread, any packets sent to the receiver by a test might not be 
in the queue of the UDPReceiver immediately afterward being sent to it. So it may be required to add a short wait
cycle to the test before checking the packets in order to let the thread catch up processing.

Matchers
========

Tourniquet comes shipped with a set of Matchers and factory methods to support writing tests using Tourniquet.

- Network Matchers
- Check availability or reachability of a TCP port
- Check availability of a URL

Network Matchers
-----------------

Check availability or reachability of a TCP port

A typical use case for using the Network Matchers is to verify the availability of a local server port before you start 
up a server that listens on that port. Another use case is to verify - once your server started up - that it is actually 
listening on that port.

To support that use case, Tourniquet provides Network Matchers, which are easily accessible through the 
```NetworkMatchers``` utility class, which can be statically imported.

It provides two methods to create a TcpPort description

    //local port
    TcpPort port = NetworkMatchers.port(80);
    //or for remote ports
    TcpPort port = NetworkMatchers.remotePort("some.remote.host",80)

That can be used in conjunction with either the EndpointMatcher oder ResourceAvailabilityMatcher accessible through

    //for the availability of a resource, like a local server port
    NetworkMatchers.isAvailable()
    //or for remote ports
    NetworkMatchers.isReachable()

These methods allows you to write assertions or assumptions that check the availability of  a port

    import static org.junit.Assume.assumeThat;
    import static org.junit.Assert.assertThat;
    import static NetworkMatchers.*;
     
    ...
     
    @Test
    public test_serverStart() {
        //given
        assumeThat(port(80), isAvailable());
        //when I start my server
        ...
        //then
        assertThat(remotePort("localhost",80), isReachable());
    }

###Check availability of a URL
To test, if a resource specified by an URL is available, you may use the  ResourceAvailabilityMatcher as well.

    import static org.junit.Assert.assertThat;
    import static NetworkMatchers.*;
     
    ...
     
    @Test
    public test_serverStart() {
        //given
        URL testResource = new URL("http://google.com");
     
        //then
        assertThat(testResource, isAvailable());
    }

 

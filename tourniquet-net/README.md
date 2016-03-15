Usage
=====

Beyond Rules and Matchers, Tourniquet provides a set of Utility classes and functions to support the creation of test
 cases.

        
Network Utils
-------------

The class NetworkUtils provides a set of methods that might come in handy when you write tests 
that depend on certain networking aspects.

###Find available Port

The use case is, that you write a test that starts up a server listening on a dedicated port. But you don't know, if the 
port you configure is available on the build agent. To be less prone to random errors, you require a way to find an 
available port and skip the test if no available port is found. The port is checked for availability both as TCP and
UDP port.

To find a random port, use

        //tries 3 times to find a random port that is not locally bound
        int port = NetworkUtils.findAvailablePort();
        //tries n times to find a random port that is not locally bound
        int n = 10;
        int port = NetworkUtils.findAvailablePort(n);

The default number of retries (used in the parameter-less method) is defined in the DEFAULT_RETRY_COUNT shared variable. 
This variable can be modified by

        System property tourniquet.net.maxRetries, i.e. as VM option
        -Dtourniquet.net.maxRetries=25

by setting it directly:

        NetworkUtils.DEFAULT_RETRY_COUNT.set(25);

### Verify availability of a port


If you have a specific port in mind and want to verify, that it is available for listening - and optionally find an 
available port. An available port is usable as UDP or TCP port.

        import static org.junit.Assume.assumeTrue;
        
        ...
        
        int port;
        if(NetworkUtils.isPortAvailable(8080)){
            port = 8080;
        } else {
            port = NetworkUtils.findAvailablePort();
        }
        
### Generate a random port

To generate a random port number call

        int port = NetworkUtils.randomPort();

Which will generate a random number without checking the availability of the port.

The method uses the range between 1024 and 65536. The lower boundary can be moved using the ```PORT_OFFSET``` variable 
which defaults to 0.

The port offset can be modified by

        System property tourniquet.net.portOffset, i.e. as VM option
        -Dtourniquet.net.portOffset=10000

by setting it directly:
        
        NetworkUtils.PORT_OFFSET.set(10000);

A note on the default settings. The variables DEFAULT_RETRY_COUNT and PORT_OFFSET are shared variables and not 
intended to be changed for each test individually but for the whole "test session" instead. Best practice - 
if changes are needed - is to set them as system property.

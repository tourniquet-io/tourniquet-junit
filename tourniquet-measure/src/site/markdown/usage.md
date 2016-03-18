# Response Time Recording
To collect the response times during a test you have to add the `ResponseTimeRecording` rule to your tests:

```java
    @Rule
    public ResponseTimeRecording rt = new ResponseTimeRecording();
```
    
This enables response time recording for the current thread. If you use the transaction support, it will also enable 
the automatic recording of all response times of performed transactions of your model.

The test rule uses the response time collector that is bound to the current thread. The ResponseTime recording mechanism
can also be used outside of a unit test, if needed, using the `ResponseTimeCollector`

```java
    ResponseTimeCollector collector = new ResponseTimeCollector();
    collector.startRecording();
    // do something
    collector.stopRecording();
```
    
This will bind the collector instance to the current thread, making it accessible using a static getter:

```java
    ResponseTimeCollector.startTx("customTx");
    //do transaction
    ResponseTimeCollector.stopTx("customTx");
```

## Accessing Response Times
All started and completed collection are collected in the `ResponseTimes` collection. All collected 
response times are accessible by transaction via the `getResponseTimes()` method.

## Custom Measure Handlers
In case you want to add some additional processing on the measure events, you may register a custom handler using the
methods:

- `onMeasureStart`
- `onMeasureEnd`

Typical use cases for changing that behavior are to pass the measures into a central database or to another processing
system. Alternatively, you define a cleanup strategy.
 
Note that using custom handlers will not change the behavior of collecting response times. 

## Global vs Local Collector
All response times are collected in the `ResponseTimes` class. The collector has a local and global instance. The
local instances are scope for a single thread. The global collector are valid for all threads. 
The default setting ist, that local collectors do not forward response times to the global collector. Instead,
response times have to be explicitly globally collected:

```java
ResponseTime rt = ResponseTimes.global().startTx("myTx");
//do some stuff
ResponseTimes.global().stopTx(rt);
```

There are some options to change that behavior:

* Global Forward
using the `enableGlobalCollection()` method, forwarding of collected times to the global collector is enabled or 
disabled for all threads
* Local Forward
using the `enableForwardToGlobal()` method, forwarding of collected times to the global collector is enabled for the
 current thread. Note that this setting has no effect on the global collector.
 
## Cleanup
In order to prevent the local or global collections from filling up, a cleanup strategy can be defined. The cleanup task
will run periodically as defined by the duration between each executions. Strategies could range from cleaning the 
entire map, pushing the times to an external source or retain only the latest measures. The default setting however,
is to do nothing. Without setting a cleanup strategy, the collection has to be cleaned manually.

Example for setting a cleanup strategy:
```java
ResponseTimes.global().setCleanupStrategy(Map::clear, Duration.ofMinutes(10));
```

## Classloader isolation
In cases where tests are executed in an isolated classloader, it might be required to get the collected response times
collected by the test. The `ResponseTimes` provides two static getter methods to retrieve the thread-local or global 
response times from the other classloader. The method ensures, the response times are cleanly transferred across
classloader boundaries.


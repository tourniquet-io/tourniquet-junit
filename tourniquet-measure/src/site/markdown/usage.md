# Response Time Recording
To collect the response times of the declared transactions of your Page Object model in your test, you have to 
add the `ResponseTimeRecording` rule to your tests:

    @Rule
    public ResponseTimeRecording rt = new ResponseTimeRecording();
    
This will activate the automatic recording of all response times of performed transactions of your model.

The test rule uses a response time collector that is bound to the current thread. The ResponseTime recording mechanism
can also be used outside of a unit test, if needed, using the `ResponseTimeCollector`

    ResponseTimeCollector collector = new ResponseTimeCollector();
    collector.startRecording();
    // do something
    collector.stopRecording();
    
This will bind the collector instance to the current thread, making it accessible using a static getter:

    ResponseTimeCollector.current().ifPresent(rtc -> rtc.startTx("customTx");
    //do transaction
    ResponseTimeCollector.current().ifPresent(rtc -> rtc.stopTx("customTx");
    
## Accessing Response Times
All started and completed collection are passed to the global collector `ResponseTimes`. The default setting for that
global collector is to record all captured response times and making them accessible using the `getResponesTimes`, 
providing a map, mapping all transactions to recorded response times for that transaction. As being a shared collector,
the collection of recorded response times fills up over time and needs to be cleaned up to prevent memory leaking.
Therefore the `clear` method should be invoked if the captured response times are processed.

This default behavior can be changed by overriding the handlers for 

- `onMeasureStart`
- `onMeasureEnd`

Typical use cases for changing that behavior are to pass the measures into a central database or to another processing
system.

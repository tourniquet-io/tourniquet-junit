# ResponseTime Collection
The response time collection framework has three main classes. 

## ResponseTime
The basic data type is the `ResponseTime` which denotes the time a transaction takes to complete. It is a `TimeMeasure`
 and associated by name with a transaction. To distinguish between multiple response times for the same transaction, 
 every `ResponseTime` has a unique id.
 
## ResponseTimeCollector
The collector provides the logic for tracking transactions for the current thread. Recording has to be manually
started and stopped. It is limited to the current thread. It may track multiple transactions with different names, 
but only one at a time with the same name. As soon as a new transaction of the same name is started the existing 
transaction of this name is aborted. Once a tracked transaction is finished, it is flushed to the `ResponseTimes` 
collection.

## ResponseTimes
The `ResponseTimes` is a central store for all measured response times - either for the current thread, or globally.
All recorded response times can be retrieved or processes. Custom response time handlers can registered.

## ResponseTimeRecording
The recording is a JUnit Rule that controls the `ResponseTimeCollector`s recording logic. Using this rule, the 
recording of response times starts and stops automatically. In case recording is externally controlled for the current
 thread, it will not change that setting. This behavior allows test runners to manage the response time handling 
 without having to parametrize the test explicitly.

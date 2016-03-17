Utilities
=========

The core module contains a set of utilities in the context of test execution. Some of these are widely used by 
tourniquet itself, other may be used outside of tourniquet for build test environments.

CallStack
---------
Contains a set of 

ResourceResolver
----------------
Locates a resource in classpath, either by absolute naming or relative to the caller or another class.

Usage
```java
URL resource = new ResourceResolver().resolve("someResource.txt");
```



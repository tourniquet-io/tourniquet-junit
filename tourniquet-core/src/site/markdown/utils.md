Utilities
=========

The core module contains a set of utilities in the context of test execution. Some of these are widely used by 
tourniquet itself, other may be used outside of tourniquet for build test environments.

CallStack
---------
Contains a set of methods to get information from the call stack.

The `getCallerClass` retrieves the class reference of the method's caller's caller, for example:

* CallerClass
    * CallerOf getCallerClass (wants to know, which class calls it)
        * getCallerClass -> returns CallerClass
        
Getting a reference to the current method is a bit trickier. The instance need to be tracked by invoking
the `track` method and operate on the result.
With that in place, you can invoke the `currentMethod` anytime to get a reference to the last method that has been
invoked on a tracked instance on the current callstack.

ClassStreams
------------
Provides methods to get a stream on the supertypes of a class so that the class hierarchy can be processed using the 
Java8 Stream API.

Example:
the following code snippet sets all public field in the class hierarchy with a new Instance of ExampleType.
```java
Object target =...;
ClassStreams.selfAndSupertypes(target.getClass())
                    .flatMap(c -> Stream.of(c.getDeclaredFields()))
                    .filter(f -> ExampleType.class.isAssignableFrom(f.getType()))
                    .forEach(f -> f.set(target, new ExampleType()));
```

ExecutionHelper and ExecutionResult
-----------------------------------
Provide a way to wrap exception handling of a method invocation so that the result as well as the exceptions can 
dealt with in a functional way. The ExecutionResult is a monad for setting result and exception of a method invocation
into a computational context.
Using the provided methods allows to use exception-throwing methods in a lambda expression without having to deal 
with exceptions directly in the expression. 

The methods `runUnchecked` will simply wrap any exception into a `RuntimeException`.

The methods `runChecked` will return an `ExecutionResult` allowing to fluently deal with exceptions or access the
result of the invocation.

Example:
```java
String result = ExecutionHelper.runProtected(() -> produceString())
                               .catchException(e -> log.error("Exception occurred", e)
                               .get();
```
Or to move the exception from the lambda expression to the caller of the overall expression:
```java
String method() throws Exception {
    return ExecutionHelper.runProtected(() -> produceString()).flatten();
}
```

JarScanner
----------
Utility for scanning a jar from a given URL to get the contained packages and classes of the jar as Strings. 

Example:
```java
Set<String> packages = new JarScanner().addJar(jar).ignore(excludePackage).scanPackages()
```

ResourceResolver
----------------
Locates a resource in classpath, either by absolute naming or relative to the caller or another class.

Usage:
```java
URL resource = new ResourceResolver().resolve("someResource.txt");
```

Isolated TestExecution
----------------------

In some situations it might be required to execute a JUnit test in an isolated classloader hierarchy. Maven surefire or 
IDE Junit runners provide an isolated test execution environment. But if required to embed test execution capabilities
outside these places, you may use the `JUnitRunner` in combination with the `TestClassLoader`

The JUnitRunner allows to run a test using a dedicated classloader. It invokes the JUnit execution engine and 
ensures, the produced results are serializable accross classloader boundaries - which is required in case the caller
classpath does not contain the tests contained in the test classpath.

Usage:
```java
try(TestClassLoader cl = new TestClassLoader(testJar)){
    Result result = JUnitRunner.runClass("my.example.Test", () -> cld);
}
```

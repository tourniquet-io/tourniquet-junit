Usage
=====

BaseRule
-----------------------------------

The BaseRule is the base class for all the Tourniquet rules. It implements the TestRule 
interface of Junit and contains the chaining mechanism. It provides access to the outer rule and the wrapped execution 
thereof. Further provides the capability of tracking the initialization of the rule to support "late" initialization 
in a test's @Before methods (and vice versa for tear-down).

Usually you don't have to deal with the BaseRule directly.

ExternalResource
-------------------------------------------

The ExternalResource is an alternative to the ExternalResource provided by JUnit. The 
ExternalResource provided by JUnit provides a default Statement wrapped around the base statement that invokes a 
overridable before and after method. The rule does not differentiate if it is a @ClassRule or a @Rule. But there are 
some occasions where the setup and teardown behavior is different when being used as class rule instead of test rule.

The external resource has a detection of the context and invokes different setup/teardown methods when being used as 
@ClassRule. Therefore it provides the empty-bodied, overridable methods

- beforeClass
- before
- after
- afterClass

As the methods are independent of each other, it is even possible to use the same rule instance as @ClassRule AND @Rule

```java
    public MyTest {
 
        @ClassRule
        public static ExternalResource tourniquetResource = ...
         
        @Rule
        public ExternalResource testResource = tourniquetResource;
         
        ...
    }
```

A typical use case scenario could be

1. beforeClass: startServer
2. before: prepareServerContent
3. do your first test
4. after: destroyServerContent
5. before: prepareServerContent
6. do your second test
7. after: destroyServerContent
8. afterClass: stopServer

This allows to optimize your test-execution time by doing time-consuming setup/teardown only once, as long as the 
potentially destructive behavior or your tests allows for doing so.

Finally, as the ExternalResource inherits from the BaseRule it supports rule chaining.

SystemProperties
-------------------------------------------

The `SystemProperties` Rule captures and restores the current system's properties before and after test execution.
Its a tool for cleaning up the JVM so it's less likely that following test fill behave differently due to changed
system properties. 
The Rule can easily be created by instantiating it and doesn't require further initialization or parameters.

```java
    @Rule
    public SystemProperties sysprops = new SystemProperties();
```
    
SystemConsole
----------------------------------------
The `SystemConsole` Rule captures the output written to `System.out` and `System.err` so that it is accessible from 
within the test case, i.e. for verifying a certain output has been written. The original streams are preserved and
restored on teardown.

The Rule can easily be created by instantiating it and doesn't require further initialization or parameters. The output
written is accesible by getter methods.

```java
    @Rule
    public SystemConsole console = new SystemConsole();
    
    @Test
    public void outTest() {
        System.out.print("test");
        assertEquals("test", console.getOut());
    }
    
    @Test
    public void errTest() {
        System.err.print("test");
        assertEquals("test", console.getErr());
    }
```

DateFormatMatcher
-----------------------------------------------

The DateFormatMatcher can be used to verify that a Date String matches a certain format using the verbose `assertThat`
syntax.

```java
    assertThat("2015-31-12", matchesDateFormat("YYYY-MM-DD"));
```

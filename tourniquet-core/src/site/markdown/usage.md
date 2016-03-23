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

    
ParameterProvider
----------------------------------------

This rule allows to parametrize a test. Different to the JUnit `Parametrized` runner, this rule provides a means to
 feed test data externalize in order to configure it for changing test environments or test dataset. It is not the
 intention to run the test with various datasets multiple times in the same test execution but to parametrize a single
 test execution only. The rule allows to specify a mapper function that provides the value for a specific parameter key.
 
```java
    @Rule
    public ParameterProvider parameters = new ParameterProvider();
    
    @Test
    public void test() {
        int value = parameters.getValue("key", int.class, 123);
    }

```
 
The default source is the `TestExecutionContext` and if that is not initialized, the parameters are read from the
 System properties.

The `TestExecutionContext` is designed for embedded test execution using the `JUnitRunner`. It encapsulates the access
to a defined set of properties which are bound to the current thread. It's also possible to initialize the context 
from the test execution and access the values using the `ParameterProvider`, although this is not recommended in 
combination with the `JUnitRunner` as the context is a means of getting properties from outside (the code that embeds 
the test execution) into the test.

```java
    @Rule
    public ParameterProvider parameters = new ParameterProvider();
    
    @Before
    public void setUp(){
        Properties props = new Properties();
        props.setProperty("key", "456");
        //init props
        TestExecutionContext.init(props);
    }
    
    @Before
    public void tearDown(){
        TestExecutionContext.destroy();
    }
    
    @Test
    public void test() {
        int value = parameters.getValue("key", int.class, 123);
    }

```

OutputCollector
----------------------------------------
The output collector is a rule to process output data generated by the test. For example, output values can be forwarded
 to external systems.
If using the `TestExecutionContext`, the default behavior of this rule is to store the values in the 
`TestExecutionContext` as output values that can be retrieved on destruction of the context.

DateFormatMatcher
-----------------------------------------------

The DateFormatMatcher can be used to verify that a Date String matches a certain format using the verbose `assertThat`
syntax.

```java
    assertThat("2015-31-12", matchesDateFormat("YYYY-MM-DD"));
```

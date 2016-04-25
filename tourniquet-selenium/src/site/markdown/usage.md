# Selenium Control
This module simplifies handling of Selenium Web Drivers in tests, page object model or other classes. It provides a 
JUnit rule that initializes a context with a webdriver. The context and the driver is accessible via static getter 
method so that it can be obtained from any class without having to provide the driver explicitly, for example through
a setter or contstructor.

## Usage

This library requires a Java 8 JRE to run. If you write code for Java 7 production environments, but
 your build and test execution environment supports Java 8 - Selenium tests can be executed in a 
 different JVM than the system under test - you may put your Selenium based tests in a separate
 Maven module, and configure only that module to compile for Java 8. I.e. by adding the compiler
 plugin configuration.

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

The package has to main components: the `SeleniumContext` providing access to the driver and test execution related
date, and the `SeleniumControl` which is a rule that handles initialization and destruction of the context. In order
to use the context in your test, you have to add the `SeleniumControl` rule to your test. The rule comes with a 
builder with fluent API. So to create an instance, use

```java
    @Rule
    public SeleniumControl control = SeleniumControl.builder()
                                                    .driver(Drivers.HEADLESS)
                                                    .baseUrl("http://localhost:80/home")
                                                    .build();
```

The `driver` parameter accepts a Supplier, the `Drivers` enum provides suppliers for the default drivers.
The `baseURL` must be present. It is used to resolve relative paths using the SeleniumContext's `resolve()` method and
is initially loaded when the rule is evaluated.

See also `io.tourniquet.pageobjects.SeleniumControlExample` in the test source.
   
# Timeouts
Besides access to the driver, the context may hold a `TimeoutProvider` which can be used to define configurable 
timeout value in order parametrize test execution behavior depending on the execution platform. This an optional 
feature, which is not required for the SeleniumContext. 


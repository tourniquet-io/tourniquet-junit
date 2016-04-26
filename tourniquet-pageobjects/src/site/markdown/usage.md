# pageobjects
Pageobjects is a helper library for defining a [Page Object Model](http://martinfowler.com/bliki/PageObject.html)
to be used with Selenium. 

## Why tourniquet-pageobjects?
Selenium itself already provides a convenient way of declaring a model, using the `By` 
locators and a set of operations on the page, for example:

```java
    public class LoginPage {
    
        WebDriver driver;
        By username = By.id("username");
        By password = By.id("password");
        By login = By.id("login");
        
        public LoginPage(WebDriver driver) {
            this.driver = driver;
        }
        
        public void enterUsername(String username){
            driver.findElement(username).sendKeys(username);
        }
        public void enterPassword(String password){
            driver.findElement(password).sendKeys(password);
        }
        public void pressLogin(){
            driver.findElement(login).click();
        }
        
    }
```

This approach is good for most cases, but require a lot of repetitive code, because typically you only want to
locate an element and either click it or enter input to it - in case of a form, you want to submit it. Further, for 
defining a fluent API on the model objects, you have to add a `return this;` On top of that, if certain elements only
appear after a certain action, you want to wait until the element to click appears. The syntax for creating a dynamic
 wait get easily quite complex and difficult to read.
 
Tourniquet helps you to reduce effort for creating a page object model and generate conventional code. Locations of 
elements and the time to wait for those are declared using annotations. But not only elements can be annotated, 
but methods as well. The default implementations for entering input or clicking elements may be omitted and 
automatically injected. 

To simplify reuse parts of the UI or its widgets, elements can be grouped and nested. Pages and ElementGroups can be 
defined as classes, where all the single elements of the page or element group can be defined using an 
annotation. The framework helps you locating the elements and injecting a default getter (Supplier) for that element, 
including waiting for the presence of the element.

On top of that, it includes support for declaring user transactions for the execution time can be measured. 
All measured times can be collected and process, i.e. for performance analysis.

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
    
# Defining a Page Object Model

The library contains two base types for declaring your page objects:

- `ElementGroup` defines an arbitrary set of elements that are logically bound together
- `Page` is a special element group declaring a page of your application. 

Both are interfaces that may be implemented by your page object model and define default methods providing
some logic for navigating and element location.
The single elements of the page or element group have to be fields of the class of type 
`java.util.function.Supplier<WebElement>` and must be annotated with `Locator`.

The `Locator` can contain a simple URL as String value which can be used to annotate a Page type in
 order to define on which url the page can be found. For all elements on a page, the type of locator
 has to be defined, which map directly to selenium By locators.

## Define a simple page

As example, take a login page:

```java
    @Locator("login.jsp")
    public class LoginPage implements Page {

        @Locator(by = ID, value = "username")
        Supplier<WebElement> username;
        
        @Locator(by = ID, value = "password")
        Supplier<WebElement> password;
                
        @Locator(by = ID, value = "login")
        Supplier<WebElement> loginButton;
        
        public LoginPage enterUsername(String username){
            username.get().sendKeys(username);
        }
        public LoginPage enterPassword(String password){
            password.get().sendKeys(username);
        }
        public void pressLogin(){
            loginButton.get().click();
        }
    }
```

## Default Interactions
In the above example for a simple page, the interactions with the elements are fairly common. Tourniquet-pageobjects
provides some convenience for dealing with such web elements and the interaction by generating the default interactions
for you. It is only required to declare an abstract method and annotate it with the locator information. The
according code is injected, depending on the parameters:

* if the element points to <form>, the `submit()` method is invoked, regardless of the parameters passed
* if no parameter is passed, the element is `click()`ed.
* if a single parameter is passed, the element is `clear()`ed and afterwards the key sequences defined by the 
parameter's  `toString()` is send as keys using the `sendKeys()` method.

The methods may return one of the following:

* void
* WebElement - the element found by the locator
* the ElementGroup or Page itself, supporting fluent DSLs

With the implicit interactions, the above example would look like this:

```java
    @Locator("login.jsp")
    public abstract class LoginPage implements Page {
        
        @Locator(by = ID, value = "username")
        public abstract LoginPage enterUsername(String username);
        
        @Locator(by = ID, value = "password")
        public abstract LoginPage enterPassword(String password);
        
        @Locator(by = ID, value = "login")
        public abstract void pressLogin();
    }
```


## Running Tests with Page Objects
The parts that load the page and locate the elements on the page require a Selenium driver. The default
scenario is to run a test with a single driver in one test execution thread. In order to initialize a driver
for the current test, the `tourniquet-selenium` module provides a test rule that has to be added to the test:

```java
    public class PageObjectTest {
    
        @Rule
        public SeleniumControl selenium = SeleniumControl.builder()
                                                         .baseUrl(basePath)
                                                         .driver(Drivers.FIREFOX)
                                                         .build();
                                                         
         //...
    
    }
```

The basePath in the example has to be the absolute URL required to resolve all relative URLs of 
locators defined of your model. For driver a `java.util.function.Supplier` is passed. The library
provides a set of default suppliers in the `Drivers` enum.

## Using the Page Objects in your test
When using the page object in your tests, you have to navigate to the page first and then may invoke
the methods on the objects.

```java
    @Test
    public void testLogin() {
        LoginPage login = Page.navigateTo(LoginPage.class);
        login.enterUsername("username")
             .enterPassword("password")
             .pressLogin();
    }
```

If the mechanism to load a page - either by navigating to the URL or by clicking on an element on the page - 
should be different, the `loadPage()` method can be implemented.

## Grouping Elements
Typically, elements of a web page are grouped into logical units. A common example is a Form element containing input
 elements. Further, Selenium supports element location in a specific search context. The default search context is 
 the driver, but every other element of a page can be a search context itself, i.e. the mentioned form element being 
 the search context for locating the input elements.
 
the pageobjects library provides the `ElementGroup` interface for that. The interface defines the `getSearchContext` 
method defaulting to the current driver. If an ElementGropu requires a different search context, this method must be
implemented. 
Further the `ElementGroup` defines a generic getter method for obtaining sub-element groups and a method that is
 invoked to locate the elements of the page, defaulting to the injection mechanism of the pageobjects library.

The `Page` interface is a specialization of the `ElementGroup`.

Any page or element group can nest further element groups. Those are automatically injected, when the test control 
flow navigates to the page and thereby creating the page instance. 

Example:

```java
    public class LoginForm implements ElementGroup {
        @Locator(by = ID, value = "username")
        Supplier<WebElement> username;)
        
        @Locator(by = ID, value = "password")
        Supplier<WebElement> password;
                
        @Locator(by = ID, value = "login")
        Supplier<WebElement> loginButton;
        
        public LoginPage enterUsername(String username){
            username.get().sendKeys(username);
        }
        public LoginPage enterPassword(String password){
            password.get().sendKeys(username);
        }
        public void pressLogin(){
            loginButton.get().click();
        }
    }
    
    @Locator("login.jsp")
    public class LoginPage implements Page {
        LoginForm form;
    }
```

   
# Timeouts
When modelling an application page model, certain elements or pageloads have to meet a respones time goal or the 
test should be guarded from taking too long. Selenium provides add timeouts to your code. The declarative approach of
Tourniquet allows you to specify timeout in the annotations and to make timeouts configurable.

## Fixed timeouts
The `@Locator` annotation allows to specify a timeout element on each element or for the page. This timeout is the 
number of milliseconds that it may take for the element to appear or the page to load. The timeout for pageload includes
the time required for rending the page - that is, waiting until the `readyState` of the document is `complete`.

To specify a fixed timeout for an element or page, simply specify the value in the annotation. The following snippet 
waits at max 30 seconds for the element to appear
 
```java
    @Locator(by = ID, value = "username", timeout=30)
    Supplier<WebElement> username;)
```

## Configurable Timeout
Sometime it may be required to parameterize the timeouts in your object model, for example to separate response time 
goals from your test code or to adapt the timeouts to the performance profile of the test environment. In that case
timeouts can be configured. The actual timeout values are provided by a `TimeoutProvider` that is bound to the 
current `SeleniumContext`. The timeouts are referred to using keys. To declare a timeout to be configurable, you have 
to specify the timeout key:

```java
    @Locator(by = ID, value = "username", timeoutKey="usernameTimeout")
    Supplier<WebElement> username;)
```

To set a timeout provider, you have to configure it in the SeleniumContext:

```java
    TimeoutProvider provider  = ...;
    SeleniumContext ctx = ...;
    ctx.setTimeoutProvider(provider);
```

The order of lookup for a timeout is (returning on the first match)

* locator timeout (by key)
* locator timeout (fixed value)
* fallback values (by key)
* fallback values (fixed value)

The fallback may depend on the timeout required, for example loading a page uses a different timeout than waiting
for an element. In any case, if no specific timeout is defined, a fixed-value default timeout is used for all.

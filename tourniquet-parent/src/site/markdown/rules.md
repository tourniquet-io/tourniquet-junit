Rules
=====

Tourniquet's set of rules is based on the TestRule feature of JUnit, that provide support for various functional 
services that are typically only available in an integrated environment. Using Tourniquet rules, allows to define test 
cases that provide embedded infrastructure that does not have to be mocked and reacts exactly as specified by the 
standards of those service.

JUnit Rules
-----------

JUnit provides the option to encapsulate setup and teardown behavior of a test into a separate class - a rule. Rules 
are applied by wrapping the test execution in a statement and execution code around that statement. Using that 
mechanism, rules can be nested in each other. In any case, rule are executed around the test, including methods 
annotated with @Before and @After.

The execution order around test code is as follows:

* @ClassRule before test execution
* @BeforeClass method
* @Rule Code before test execution
* @Before method
* @Test method
* @After method
* @Rule Code after test execution
* @AfterClass method
* @ClassRule after test execution


One of the consequence of this execution order is, that you can not setup you test rule in a @Before annotated method 
if the setup data is required for the rule execution. Actually, there is no chance of calling any setup method for the 
rule before it is executed.

### Chaining Rules

If you're using multiple rules, simply declaring them as separate rules does not guarantee any specific execution order. 
In fact, it can be rather arbitrary.
    
```java
    @Rule
    public TestRule rule1;
    @Rule
    public TestRule rule2;
    @Rule
    public TestRule rule3;
```

So in order to combine multiple test rules and execute them in a specific order, JUnit offers a rule named RuleChain 
which can be created like

```java
    @Rule
    public TestRule chain = RuleChain.outerRule(new LoggingRule("outer rule")
                                     .around(new LoggingRule("middle rule")
                                     .around(new LoggingRule("inner rule");
```

But JUnit comes with no specific support for interdependent rules. To create rules that depend on the reference to 
another rule AND the execution of the rule before (or after) the rule, you have to instantiate the rules first and then 
define them as a rule chain:

```java
    //no @Rule annotation!
    public FirstRule firstRule = new FirstRule;
    //create a reference to firstRule
    public SecondRule secondRule = new SecondRule(firstRule);
    //create the rule chanin - with @Rule annotation
    @Rule
    public TestRule chain = RuleChain.outerRule(firstRule).around(secondRule);
```

It is obvious that relying on more than two rule can get cumbersome and decrease the readability of your test code.

### Tourniquet Rules

Basically, Tourniquet relies on the same mechanism but provides a more convenient way for defining rules - a builder!

Tourniquet consists of a set of functional rules that can be chained and referenced in one single-line statement. 
Every method invocation creates or configures a builder which allows to create references among the rules, chain the 
rules and setup the rules before.

* Basic Rules
* JCR Repository Rules
* LDAP Server

The API design conventions for Builders are

* methods starting with new create a builder (only used in factory class Tourniquet)
* methods starting with around create a builder for chained (nested) rule
* method build builds the rule (see io.tourniquet.junit.rules.builder.Builder)
* all other methods configure the builder

The Tourniquet BaseRule provides support for defining a dependency relationship between an 
outer and an inner rule.

The source-level RuleSetup annotation is used to indicate a rule method that can be 
used to set up a rule and is intended to be called by a builder.

Usage
=====

With the Tourniquet JCR Rules, you can create test cases that may interact with a standards conforming JCR repository. 
Tourniquet provides a set of various JCR repository implementations to support various use cases.

All repository rule implementation rely on the Jackrabbit 2 reference implementation of the JSR 283. Although there 
is a successor implementation Apache OAK available, the decision to use Jackrabbit 2 was driven mainly, because it 
provides a complete implementation of the specification, including the optional aspects - which might be relevant 
when writing test cases. OAK aims at scalability and performance at the cost of leaving out some optional elements 
(i.e. support for same-name sibblings), and scalability and performance might be less important for unit testing.

Repository Rules
----------------

All the repository rules inherit from ```io.tourniquet.junit.rules.jcr.ContentRepository``` that provides the 
following default functions

- ```getRepository()``` access to the javax.jcr.Repository instance managed by the rule.
- ```login(username, password)``` login to the underlying repository using the given credentials to obtain a javax.jcr.Session
- ```login(username, password)``` login to the underlying repository using the given credentials to obtain a javax.jcr.Session
- ```getAdminSession()```  login to the underlying repository to obtain a javax.jcr.Session with administrator privileges. 
The session is reused and refreshed on every call of the method. The method uses the default credentials "admin"/"admin".
- ```getWorkingDirectory()``` access to  the outer rule TemporaryFolder that provides access to the working directory in the
 file system
- ```getInjectionValue()``` provides Injection support by returning the javax.jcr.Repository to be injected into a target 
object

### InMemoryContentRepository

The in-memory content repository rule creates a pre-configured Repository that has in-memory persistence and filestore. 
All of it's contents is gone once the repository is shut-down. The in-memory repository is using the TransientRepository 
of Jackrabbit that automatically shuts down the repository when the last session has been closed.

The in-memory repository requires a working directory, so you need to create it in a TemporaryFolder.

    @Rule
    public ContentRepository repository = Tourniquet.newTempFolder().aroundInMemoryContentRepository().build();

The default configuration has no effective security enabled as it uses Jackrabbit's SimpleSecurityManager

### StandaloneContentRepository

The StandaloneContentRepository offers the same persistance as a real JCR repository. It creates the file structure for 
the repository inside a temporary folder and starts up the repository.Unline the In-Memory Repository, this repository 
is not shut down when the last session is closed. The default configuration uses the same in-memory persistence 
configuration as the InMemoryContentRepository, but it can be changed to use a custom configuration with configured
persistence so that you can start and shutdown the repository during the test without loosing any data stored.

To create a standalone repository with the default configuration apply the following tourniquet

    @Rule
    public ContentRepository repository = Tourniquet.newTempFolder()
                                                  .aroundStandaloneContentRepository()
                                                  .build();

To create a standalone repository with a custom configuration accessible through an URL apply the following tourniquet

    @Rule
    public ContentRepository repository = Tourniquet.newTempFolder()
                                                  .aroundStandaloneContentRepository()
                                                  .withConfiguration(configURL)
                                                  .build();

The default configuration has no effective security enabled as it uses Jackrabbit's SimpleSecurityManager

### JNDIContentRepository (experimental)

The JNDI Content Repository allows to provide access to the repository using a JNDI context lookup. The rule allows to 
use an existing context, set up the context with properties and define a lookup name.

The default lookup name for the JCR repository is ```java:/jcr/local```.

The use case for this rule is to test inside an integrated environment such as an Application Server, where a 
pre-configured repository is accessible through Java naming lookups, i.e. when doing integration testing with Arquillian.

####Default Lookup

To create a test rule for a content repository available through the default lookup name and using the default 
InitialContext apply the following tourniquet

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository().build();

#### Custom Lookup name

To create a test rule for a content repository available through a custom lookup name and using the default 
InitialContext apply the following tourniquet

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withLookup("java:/custom/lookup/name")
                                                  .build();
#### Context handling

To use an existing context

    private Context context;
     
    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .usingContext(context)
                                                  .build();

To use a specific context factory

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withInitialContextFactory("some.package.Factory")
                                                  .build();
or

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withInitialContextFactory(some.package.Factory.class)
                                                  .build();

To implicitly create an initial context factory using custom properties

    private Properties properties = ...;
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withContextProperties(properties)
                                                  .build();
or

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withContextProperty("aName", aValue)
                                                  .withContextProperty("bName", bValue)
                                                  .build();

And of course you can combine everything

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withInitialContextFactory(some.package.Factory.class)
                                                  .withContextProperty("aName", aValue)
                                                  .withContextProperty("bName", bValue)
                                                  .withLookup("java:/custom/lookup/name")
                                                  .build();
#### Remote Naming
The JNDI Content Repository builder supports to specify a remote naming provider and a principal for accessing the 
remote naming services

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withProviderURL(someURL)
                                                  .withSecurityPrincipal("principalName", "credentials")
                                                  .build();

A more concrete example for JBoss' remote naming service would be

    @Rule
    public ContentRepository repository = Tourniquet.newJNDIContentRepository()
                                                  .withProviderURL("remote://localhost:4447")
                                                  .withSecurityPrincipal("remote", "secret")
                                                  .build();

There are some limitations to remote naming. Remote naming is only supported if the transferred objects are serializable. 
Which is not the case when using the Jackrabbit JCA connector which does not support remote naming!

_So consider the remote naming support of this test rule to be experimental only!_
 
### MockContentRepository

The mock content repository simply provides access to a mock repository created using Mockito. The repository has to be 
set up manually using the known when...thenReturn or doReturn().when()... statements.

The mock repository does not require a working directory so it can be created directly from Tourniquet's factory method

    @Rule
    public ContentRepository repository = Tourniquet.newMockContentRepository().build();

User Management
---------------

User mangement of JCR repositories is not mandated by the JCR spec and is therefore up to the JCR implementation. 
The base rule for all Tourniquet ContentRepositories defines the methods for adding and removing a user. However, only 
the implementation specific rules such as InMemoryContentRepository and StandaloneContentRepository have these methods 
implemented as they both rely on the reference implementation Jackrabbit. All other ContentRepository rules will throw 
an UnsupportedOperationException.

### Adding a user

To add a user to the repositories user management, invoke

    Principal user = repository.addUser("username", "password");

The returned principal reflects the created user.

### Removing a user

To remove an existing user, invoke

### boolean success = repository.removeUser("username");

The method will return true if the user was found and could be deleted and false if no such user was found. The 
method will fail with an AssertionError if the delete operation failed for any repository-internal reason.

### Resetting users

In case you're using the repository as a class rule, you might want to reset the created users after each test. 
The rule implementation keep track of the created uses so a call to resetUsers()will remove all users created using the 
test rule's methods.

    repository.resetUsers();

Access Control
--------------

The ContentRepository rule provides methods for user management and access control. Some of these methods are specifc 
to the JCR implementation and are thefore not implemented (throwing UnsupportedOperationException). Only those methods, 
that rely on pure JCR API are implemented.

### Granting privileges

To grant a user a specific permission, you have to invoke

    repository.grant("userid", "/path/to/node", "privilege");

You may pass multiple privileges, i.e. jcr:read , jcr:write, the parameterer is vararg String. An complete list of all 
the privileges defined in the JCR specification can be found here JCR: Access Control Management.

The method uses only JCR API calls and is therefore available for all repositories.

### Denying privileges

Denying a privilege is the opposite of granting. A user may be explicitly denied to perform a certain action on a node, 
although the privilege is granted on one of the parent nodes. The syntax is similar to granting a permission:

    repository.deny("userid", "/path/to/node", "privilege");

The deny() operation is only available on the InMemoryContentRepository and the StandaloneContentRepository.

### Resetting Access Control List

To remove all access control list (ACL) entries on a single node for either a single user, multiple users or all users, 
the repository rule provides the clearACL() method.

To clear the ACLs for a single user, invoke:

    repository.clearACL("/path/to/node", "userId");

To clear the ACLs for a multiple users, invoke:

    repository.clearACL("/path/to/node", "user1", "user2", "user3",...);

To clear the ACLs for all users, invoke
    
    repository.clearACL("/path/to/node");

A word of warning, removing the ACLs on the root node "/" for all users may render the repository useless as even the 
administrator ACLs get removed. Use with caution in conjunction with standalone repositories with real persistence.

Utility Rules
--------------

### ActiveSession

With the active session you can automatically log in to a repository during test setup so you have an authenticated 
session easily available. The ActiveSession can be nested inside any of the Repository rule

    @Rule
    public ContentRepository repository = Tourniquet.newTempFolder()
                                                  .aroundInMemoryContentRepository()
                                                  .aroundSession("username", "password")
                                                  .build();

Note that when you're using the In-Memory Content Repository, logging out the active session will shut-down the repository!

### ContentLoader

see [here](content-loader.html) 

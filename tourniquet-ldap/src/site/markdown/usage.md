Usage
=====

Tourniquet provides an embedded LDAP server than can be started, stopped and configured as a test rule to provide a LDAP 
conforming server to test your LDAP code.

The LDAP Rules use the embeddable directory service and server of the Apache Directory Project.

Rules
-----
The LDAP server can be created and configured using a builder.

### Directory

The Directory rule provides the directory structure that can be access via the LDAP API of the Apache Directory project 
without having to connect to an LDAP server. The rule can be configured to serve pre-defined content.

To create the rule, use the following

    @Rule
    public Directory directory = Tourniquet.newDirectory().build();

#### Importing LDIF

To prepare the directory with content from an LDIF file, make the following call while constructing the directory.

    private final URL ldif = YourTest.class.getResource("test.ldif");
 
    @Rule
    public final Directory directory = Tourniquet.newDirectory()
                                               .withPartition("test", "cn=test")
                                               .importLdif(ldif)
                                               .build();

To import content from the LDIF, the root entry in the LDIF file must be present as a partition in the directory. 
The rule itself will use in-memory partitions. Each partition must have an ID and a context DN.

You may specify only one LDIF to be imported during rule application (setup), but you may import additional LDIF files 
and create additional partition from within the test or test-setup methods.

    @Before
    public void setup() throws Exception {
        directory.addPartition("test2", "cn=test2");
        directory.importLdif(ldif.openStream());
    }

#### Access Control and Anonymous Access

Per default, the directory has no access control enabled, allowing every user to modify existing entries. This suits the 
most test cases fine, as the typical use case is read-only access to LDAP services. Anonymous access is allowed as well.

To change these settings, you may invoked the according methods during rule creation:

    //enable access control
    @Rule
    public final Directory directory = Tourniquet.newDirectory().accessControlEnabled().build();
     
    //disable anonymous access
    @Rule
    public final Directory directory = Tourniquet.newDirectory().anonymousAccessDisabled().build();

You may not change these settings once the rule is applied.

Defaults:

- Access Control is disabled
- Anonymous Access is enabled

### DirectoryServer

The DirectoryServer rule is a wrapper for the Directory serving it's content on a TCP port using the LDAP protocol. 
It's best suited for writing tests for code that has to connect and interact directly with an LDAP server. The builder 
for the rule allows to set the port and hostname.

    @Rule
    public final DirectoryServer ldapServer = Tourniquet.newDirectoryServer()
                                                      .onListenAddress("localhost")
                                                      .onPort(389)
                                                      .build();

Default Values:

- listen address: localhost
- port: 10389

If you are not sure, whether the default port 10389 is available on your tests server, you can instruct the rule to 
select an arbitrary available port:

    @Rule
    public final DirectoryServer ldapServer = Tourniquet.newDirectoryServer().onAvailablePort().build();

You may retrieve the generated port using the getTcpPort() method on the rule.

#### Access to DirectoryService via API

To access the DirectoryService, i.e. for adding content to it, you may invoke

    DirectoryService directory = ldapServer.getDirectoryService();

Which will give you direct access to the Apache Directory Service API.

#### Initialization with Content

If you want to initialize the directory server with specific content, you have to configure the underlying Directory 
rule first and then configure the server rule. But you can do it just in one line, for example:

    @Rule
    public final DirectoryServer ldapServer = Tourniquet.newDirectory()
                                                      .withPartition("test", "cn=test")
                                                      .importLdif(ldifURL)
                                                      .accessControlEnabled()
                                                      .aroundDirectoryServer()
                                                      .onAvailablePort().build();

Which will create an access-control-enabled ldap service on an available TCP port with a partition on the context 
DN "cn=test" with content from an external LDIF file.

The "aroundDirectoryServer()" may seem semantically a bit odd, but the Directory rule has to be created first and then 
been assigned to the server. From a JUnit RuleChain perspective the rule that has to be initialized first is wrapped 
around the rule that is initialized next.

Troubleshooting
---------------

### Duplicate Schemas

When using Tourniquet's embedded LDAP server in the same module with the ApacheDS client API, the following error may 
occur:

    Caused by: org.apache.directory.api.ldap.schema.extractor.UniqueResourceException: Problem locating LDIF file in schema repository
    Multiple copies of resource named 'schema/ou=schema/cn=apachedns/ou=syntaxes.ldif' located on classpath at urls
        jar:file:/E:/mvnrepository/org/apache/directory/api/api-ldap-schema-data/1.0.0-M31/api-ldap-schema-data-1.0.0-M31.jar!/schema/ou%3dschema/cn%3dapachedns/ou%3dsyntaxes.ldif
        jar:file:/E:/mvnrepository/org/apache/directory/server/apacheds-all/2.0.0-M20/apacheds-all-2.0.0-M20.jar!/schema/ou%3dschema/cn%3dapachedns/ou%3dsyntaxes.ldif
    
The reason is, that the ApacheDS server (which is a dependency of Tourniquet) as well was the Client API have both 
the api-ldap-schema-data embedded.

So this module have to be excluded from the dependencies. Instead of using the *-all artifact, you should add the 
client-api dependency instead - which is sufficient for the most cases - and exclude the schema data module
pom.xml with basic LDAP client api

    <dependency>
        <groupId>org.apache.directory.api</groupId>
        <artifactId>api-ldap-client-api</artifactId>
        <version>1.0.0-M31</version>
        <exclusions>
            <exclusion>
                <groupId>org.apache.directory.api</groupId>
                <artifactId>api-ldap-schema-data</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

At the moment, there is no way to exclude this jar from the Tourniquet module, as tourniquet uses the server's *-all 
artifact.

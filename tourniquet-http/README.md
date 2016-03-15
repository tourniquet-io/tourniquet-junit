Usage
=====

The HTTP Module contains an embedded http server whose response behavior can be configured to match the requirements
for the test. 

HttpServer
------------------------------------------
An embedded HTTP Server provided as a test rule. The server is started before the test execution and stopped 
afterwards and may be used as a classrule or per-test rule.

###Configuration
Unless otherwise defined, the server will be started on a random available TCP port and will listen on the hostname 
`localhost`.
```java
    @Rule
    HttpServer http = new HttpServer();
``` 

Creating the server with custom port and hostname.
```java
    @Rule
    HttpServer http = new HttpServer("someHost", 8080);
``` 
Creating the server using the builder API.
```java
    @Rule
    HttpServer http = new HttpServerBuilder().hostname("someHost").port(8080).build();
``` 

### Content definition
The server has several options for the defining the response content. 

- the content from an existing zip file in the classpath or via URL
- the content from an existing single file in the classpath or via URL, hostsed on a specific path
- the content from a `TemporaryFolder`
- the content from a `TemporaryFile` created dynamically during test execution
- the content from a `TemporaryZipFile` created dynamically during test execution
- the content of a String defined a fluent stubbing API

Following are two example, more examples can be found in the test sources of the project, in the package 
`io.tourniquet.junit.http.rules.examples` 

###Definition via TemporaryZipFile
Defining the content via zip file creates a zip file using the `TemporaryFile` rule. The root of the zip file matches
the context root defined by path while all files in the zip files are entries relative to the context root.

```java
    public TemporaryFolder folder = new TemporaryFolder();
    public TemporaryFile content = new TemporaryFileBuilder(folder, "content.zip")
                                            .asZip()
                                            .addClasspathResource("/index.html","index.html")
                                            .build();
    public HttpServer server = new HttpServerBuilder().contentFrom("/", content).build();
    @Rule
    public RuleChain rule = RuleChain.outerRule(folder).around(content).around(server);

    @Test
    public void testHttpServerGet() throws Exception {
        try (final WebClient webClient = new WebClient()) {

            final HtmlPage page = webClient.getPage(server.getBaseUrl() + "/index.html");
            final String pageAsXml = page.asXml();
            final String pageAsText = page.asText();

            assertEquals("Test Content", page.getTitleText());
            assertTrue(pageAsXml.contains("<body>"));
            assertTrue(pageAsText.contains("Test Content Body"));
        }
    }
```

####Definition via Stubbing API
If the server is created with the constructor and not with the builder, the provided content can only be defined 
using the stubbing API. 

```java
    @Rule
    HttpServer http = new HttpServer();
    
    @Test
    public void testHttpServerGet() throws Exception {
        //define content via stubbing
        http.onGet("/index.html").respond("someContent");
        
        //test using html unit
        try (final WebClient webClient = new WebClient()) {
            final TextPage page = webClient.getPage(http.getBaseUrl() + "/index.html");
            assertEquals("someContent", page.getContent());
        }
    }
``` 

### Limitations
Currently only the GET method is supported.

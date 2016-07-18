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
The behavior of the server can be defined using a stubbing API which allows fine grained behavior configuration on
 various criteria. Apart from this, if the server is created with the constructor and not with the builder, the 
 provided content can only be defined using the stubbing API. 
 
Stubbing is started by invoking the `on()` method on the server, passing the expected `HttpMethod`. With the fluent
 API various matching criteria like resource path - with or without query, parameters or body can be chosen. Stubbing
 is finished by either invoking the `respond()` method, defining fixed content to be returned or `execute()` allowing
 full access on the request and response objects, allowing to dynamically react on the reuqest, if that is needed.

**Examples:**

* Defining static content for GET requests
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

* Handling query parameter
```java
    @Rule
    HttpServer http = new HttpServer();
    
    @Test
    public void testHttpServerGet() throws Exception {
        //define content via stubbing
        http.onGet("/index.html?queryParam=value").respond("someContent");
        
        //test using html unit
        try (final WebClient webClient = new WebClient()) {
            final TextPage page = webClient.getPage(http.getBaseUrl() + "/index.html");
            assertEquals("someContent", page.getContent());
        }
    }
```

* Handling answering to POST requests
```java
    @Rule
    HttpServer http = new HttpServer();
    
    @Test
    public void testHttpServerPost() throws Exception {
        //prepare
        server.on(POST).resource("/action.do").execute(x -> {
            try {
                x.getOutputStream().write("someContent".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do"))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }
    }
```

* Match a specific payload body
```java
    @Rule
    HttpServer http = new HttpServer();
    
    @Test
    public void testHttpServerPost() throws Exception {
        //prepare
        byte[] data = "Test Content".getBytes();
        server.on(POST).resource("/action.do").withPayload(data).respond("someContent");

        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do", data))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }
    }
```

* Match form parameters
```java
    @Rule
    HttpServer http = new HttpServer();
    
    @Test
    public void testHttpServerPost() throws Exception {
        //prepare
        server.on(POST)
              .resource("/action.do")
              .withParam("field1", "value1")
              .withParam("field2", "value2")
              .respond("someContent");

        //act
        try (CloseableHttpClient client = HttpClients.createDefault();
             CloseableHttpResponse response = client.execute(post("http://localhost:55555/action.do",
                                                                  param("field1", "value1"),
                                                                  param("field2", "value2")))) {
            String content = getString(response.getEntity());
            assertEquals("someContent", content);
        }
    }
```

### Limitations
Currently only the GET and POST methods is supported. POST is only configurable via Stubbing API.

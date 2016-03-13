Usage
=====

The files module contains rules for creating temporary files.

TemporaryFile
----------------------------------------
Different to the `TemporaryFolder` of Junit, the temporary file creates a concrete file on the filesystem. The
file may have content or not. The rule requires a `TemporaryFolder` in which the file is created and which ensures
the file is properly cleaned up on exit.

### Creating an empty file

```java
    public TemporaryFolder folder = new TemporaryFolder();
    
    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.txt").build();

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);
    
    @Test
    public void testZipStructure() throws Exception {
        File f = file.getFile();
        assertTrue(f.exists());
    }
```
            
### Creating a file with content

```java
    public TemporaryFolder folder = new TemporaryFolder();
    
    //external resource
    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.txt")
                                    .fromResource(new URL("..."))
                                    .build();
    //or from classpath resource (absolute or relative to test)
    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.txt")
                                    .fromClasspathResource("contentfile.txt")
                                    .build();

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);
    
    @Test
    public void testZipStructure() throws Exception {
        File f = file.getFile();
        assertTrue(f.exists());
        try(InputStream is = f.toURI().toURL().openStream()){
            String content = IOUtils.toString(is);
            assertEquals("example content from contentfile.txt", content);
        }
    }
```
            
The method `withContent()` on the `TemporaryFileBuilder` ensures that the content URL is set. If using a classpath
resource, the call can be ommitted as classpath resources are checked for existence and the test will fail, if the
resource does not exists. When using the `fromResource(URL)`, the URL may be null, the temporary file will remain
empty in that case. If the `withContent()` method is invoked, however, the test will fail if the URL is null.


TemporaryZipFile
-------------------------------------------

This rule can be used, to dynamically create zip files from a set of resources, which is automatically deleted after
the test.

### Creating a Zip from a TemporaryFile
 
```java
    public TemporaryFolder folder = new TemporaryFolder();

    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.zip")
                                        fromClasspathResource("exampleTestContent1.txt")
                                        .asZip()
                                        .build();

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);

    @Test
    public void testZipStructure() throws Exception {
        ZipFile zf = new ZipFile(file.getFile());
        assertNotNull(zf.getEntry("exampleTestContent1.txt"));
    }
```
    
Creating a zip directly from the `TemporaryFileBuilder` will create a zip with the resource specified. The file
will have the same name as the resource and will be located in the root of the zip. More files can be added
to the zip using the fluid api.

### Creating a Zip with an empty folder

```java
    public TemporaryFolder folder = new TemporaryFolder();

    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.zip")
                                        .asZip()
                                        .addFolder("/emptyFolder")
                                        .build();

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);

    @Test
    public void testZipStructure() throws Exception {
        ZipFile zf = new ZipFile(file.getFile());
        ZipEntry entry = zf.getEntry("emptyFolder/");
        assertNotNull(entry);
        assertTrue(entry.isDirectory());
    }
```

Note that the name of the folder being added differs from the folder being checked in the test. The reason is, inside
the zip, every entry - regardless of being a file or a folder - starts without a leading '/' and still denotes an
absolute path. The leading '/' while adding the folder to the zip is optional, it can be ommitted.

When reading the zip entries from the zip, the path to the zip entry must not start with a '/' otherwise it won't be
found. On top of this, it must have a trailing '/' (i.e. "emptyFolder/"), otherwise it won't be recognized as a 
directory entry. However, the entry would still be found with the trailing '/', but would not be recognized as 
directory.

### Creating a Zip with multiple files in folders

```java
    public TemporaryFolder folder = new TemporaryFolder();

    public TemporaryFile file = new TemporaryFileBuilder(folder, "example.zip")
                                        .asZip()
                                        .addClasspathResource("/text1.txt","exampleTestContent1.txt")
                                        .addClasspathResource("/test/text2.txt","exampleTestContent2.txt")
                                        .build();

    @Rule
    public RuleChain chain = RuleChain.outerRule(folder).around(file);

    @Test
    public void testZipStructure() throws Exception {
        ZipFile zf = new ZipFile(file.getFile());
        assertNotNull(zf.getEntry("text1.txt"));
        assertNotNull(zf.getEntry("/test/text2.txt"));
    }
```
    
Resources in the classpath are resolved either as absolute resources or relative to the class itself, including relative
subfolders. The paths to the files are created automatically, its not necessary to create them explicitly.

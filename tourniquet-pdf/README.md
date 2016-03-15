Usage
=====

The PDF module contains matchers for verifying PDF documents which are the result of a PDF processing. The module
provides a wrapper for a PDF source that hides the underlying PDF processing library (which is Apache PDFBox at
the moment).
The matchers provided are rather limited at the moment, but will increase in the future.

Examples
--------

Source of a PDF can be one of the following and is referred to as `source` in the following examples
- byte[]
- java.io.InputStream
- java.io.File
- java.nio.file.Path
- java.net.URL
- java.lang.String

```java
import PDF;
import static PDFMatchers.*;
```

```java
    assertThat(PDF.of(source), isPDF());
```

```java
    assertThat(PDF.of(source), hasPages(3));
```

```java
    assertThat(PDF.of(source), conformsTo(PDFALevel.PDFA_1B));
```

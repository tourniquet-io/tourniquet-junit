/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.tourniquet.junit.http.rules;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.undertow.connector.ByteBufferPool;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.util.MimeMappings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 08.12.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class FileSystemResourceTest {

    /**
     * The class under test
     */
    @InjectMocks
    private FileSystemResource subject;

    @Mock
    private Path path;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private FileSystemProvider fileSystemProvider;

    @Mock
    private BasicFileAttributes basicFileAttributes;

    @Mock
    private MimeMappings mimeMappings;

    @Mock
    private DirectoryStream<Path> directoryStream;

    @Mock
    private Sender sender;

    @Mock
    private ServerConnection serverConnection;

    @Mock
    private ByteBufferPool byteBufferPool;

    @Mock
    private PooledByteBuffer pooledByteBuffer;

    @Mock
    private IoCallback ioCallback;

    @Before
    public void setUp() throws Exception {

        when(path.getFileSystem()).thenReturn(fileSystem);
        when(fileSystem.provider()).thenReturn(fileSystemProvider);
        when(fileSystemProvider.readAttributes(eq(path),
                                               eq(BasicFileAttributes.class),
                                               (LinkOption[]) anyVararg())).thenReturn(basicFileAttributes);

    }

    @Test(expected = IllegalArgumentException.class)
    public void test_constructorWithNull_exception() throws Exception {
        //prepare

        //act
        new FileSystemResource(null);

        //assert

    }


    @Test
    public void testGetPath() throws Exception {
        //prepare
        when(path.toString()).thenReturn("/somePath");

        //act
        String result = subject.getPath();

        //assert
        assertEquals("/somePath", result);
    }

    @Test
    public void testGetLastModified() throws Exception {
        //prepare
        FileTime fileTime = FileTime.fromMillis(123456L);
        when(fileSystemProvider.readAttributes(eq(path),
                                               eq(BasicFileAttributes.class),
                                               (LinkOption[]) anyVararg())).thenReturn(basicFileAttributes);
        when(basicFileAttributes.lastModifiedTime()).thenReturn(fileTime);

        //act
        Date date = subject.getLastModified();

        //assert
        assertNotNull(date);
        assertEquals(new Date(123456L), date);
    }

    @Test
    public void testGetLastModifiedString() throws Exception {
        //prepare
        FileTime fileTime = FileTime.fromMillis(123456L);
        when(basicFileAttributes.lastModifiedTime()).thenReturn(fileTime);

        //act
        String lmString = subject.getLastModifiedString();

        //assert
        assertEquals("1970-01-01T00:02:03.456Z", lmString);
    }

    @Test
    public void testGetETag() throws Exception {
        //prepare

        //act
        assertNull(subject.getETag());

        //assert
    }

    @Test
    public void testGetName() throws Exception {
        //prepare
        Path filename = mock(Path.class);
        when(path.getFileName()).thenReturn(filename);
        when(filename.toString()).thenReturn("testfile");

        //act
        String name = subject.getName();

        //assert
        assertEquals("testfile", name);

    }

    @Test
    public void testGetName_noFilename_null() throws Exception {
        //prepare
        when(path.getFileName()).thenReturn(null);

        //act
        String name = subject.getName();

        //assert
        assertNull(name);

    }

    @Test
    public void testIsDirectory_true() throws Exception {
        //prepare
        when(basicFileAttributes.isDirectory()).thenReturn(true);

        //act
        boolean isDirectory = subject.isDirectory();

        //assert
        assertTrue(isDirectory);
    }

    @Test
    public void testIsDirectory_false() throws Exception {
        //prepare
        when(basicFileAttributes.isDirectory()).thenReturn(false);

        //act
        boolean isDirectory = subject.isDirectory();

        //assert
        assertFalse(isDirectory);
    }

    @Test
    public void testList_file_emptyList() throws Exception {
        //prepare
        when(basicFileAttributes.isDirectory()).thenReturn(false);

        //act
        List<Resource> result = subject.list();

        //assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testList_dir_dirContent() throws Exception {
        //prepare
        final Path child1 = mock(Path.class);
        final Path child2 = mock(Path.class);
        when(basicFileAttributes.isDirectory()).thenReturn(true);
        when(fileSystemProvider.newDirectoryStream(eq(path), any(DirectoryStream.Filter.class))).thenReturn(
                directoryStream);
        when(directoryStream.iterator()).thenReturn(Arrays.asList(child1,child2).iterator());

        //act
        List<Resource> result = subject.list();

        //assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(child1, result.get(0).getFilePath());
        assertEquals(child2, result.get(1).getFilePath());
    }

    @Test
    public void testGetContentType_noFilename_null() throws Exception {
        //prepare
        when(path.getFileName()).thenReturn(null);

        //act
        String contentType = subject.getContentType(mimeMappings);

        //assert
        assertNull(contentType);
    }

    @Test
    public void testGetContentType_noExtension_null() throws Exception {
        //prepare
        Path filename = mock(Path.class);
        when(path.getFileName()).thenReturn(filename);
        when(filename.toString()).thenReturn("testfile");

        //act
        String contentType = subject.getContentType(mimeMappings);

        //assert
        assertNull(contentType);
    }

    @Test
    public void testGetContentType_extension_fromMap() throws Exception {
        //prepare
        Path filename = mock(Path.class);
        when(path.getFileName()).thenReturn(filename);
        when(filename.toString()).thenReturn("testfile.txt");
        when(mimeMappings.getMimeType("txt")).thenReturn("text/plain");

        //act
        String contentType = subject.getContentType(mimeMappings);

        //assert
        assertEquals("text/plain", contentType);
    }

    @Test
    public void testServe_noException() throws Exception {
        //prepare
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        final HttpServerExchange exchange = new HttpServerExchange(serverConnection);
        when(serverConnection.getByteBufferPool()).thenReturn(byteBufferPool);
        when(byteBufferPool.allocate()).thenReturn(pooledByteBuffer);
        when(pooledByteBuffer.getBuffer()).thenReturn(buffer);
        final InputStream is = new ByteArrayInputStream("Test".getBytes());
        when(fileSystemProvider.newInputStream(eq(path), (OpenOption[]) anyVararg())).thenReturn(is);

        //act
        subject.serve(sender, exchange, ioCallback);

        //assert
        verify(ioCallback).onComplete(exchange, sender);
        buffer.rewind();
        byte[] data = new byte[4];
        buffer.get(data);
        assertEquals("Test", new String(data));

    }

    @Test
    public void testServe_withException() throws Exception {
        //prepare
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        final HttpServerExchange exchange = new HttpServerExchange(serverConnection);
        when(serverConnection.getByteBufferPool()).thenReturn(byteBufferPool);
        when(byteBufferPool.allocate()).thenReturn(pooledByteBuffer);
        when(pooledByteBuffer.getBuffer()).thenReturn(buffer);
        when(fileSystemProvider.newInputStream(eq(path), (OpenOption[]) anyVararg())).thenThrow(IOException.class);

        //act
        subject.serve(sender, exchange, ioCallback);

        //assert
        verify(ioCallback).onException(eq(exchange), eq(sender), any(IOException.class));
    }

    @Test
    public void testGetContentLength() throws Exception {
        //prepare
        when(basicFileAttributes.size()).thenReturn(Long.valueOf(123L));

        //act
        Long length = subject.getContentLength();

        //assert
        assertEquals(Long.valueOf(123L), length);
    }

    @Test(expected = AssertionError.class)
    public void testGetContentLength_exceptoin() throws Exception {
        //prepare
        when(basicFileAttributes.size()).thenThrow(IOException.class);

        //act
        subject.getContentLength();
    }

    @Test
    public void testGetCacheKey() throws Exception {
        //prepare

        //act
        assertNull(subject.getCacheKey());

        //assert

    }

    @Test
    public void testGetFile() throws Exception {
        //prepare
        File file = mock(File.class);
        when(path.toFile()).thenReturn(file);

        //act
        File result = subject.getFile();

        //assert
        assertEquals(file, result);
    }

    @Test
    public void testGetFilePath() throws Exception {
        //prepare

        //act
        Path result = subject.getFilePath();

        //assert
        assertEquals(path, result);
    }

    @Test
    public void testGetResourceManagerRoot() throws Exception {
        //prepare
        Path root = mock(Path.class);
        File rootFile = mock(File.class);
        when(root.toFile()).thenReturn(rootFile);
        when(path.getRoot()).thenReturn(root);

        //act
        File result = subject.getResourceManagerRoot();

        //assert
        assertEquals(rootFile, result);
    }

    @Test
    public void testGetResourceManagerRoot_noRoot_null() throws Exception {
        //prepare
        when(path.getRoot()).thenReturn(null);

        //act
        File result = subject.getResourceManagerRoot();

        //assert
        assertNull(result);
    }


    @Test
    public void testGetResourceManagerRootPath() throws Exception {
        //prepare
        Path root = mock(Path.class);
        when(path.getRoot()).thenReturn(root);

        //act
        Path result = subject.getResourceManagerRootPath();

        //assert
        assertEquals(root, result);
    }

    @Test
    public void testGetUrl() throws Exception {
        //prepare
        URI uri = new URI("file:///test");
        when(path.toUri()).thenReturn(uri);

        //act
        URL result = subject.getUrl();

        //assert
        assertEquals(new URL("file:///test"), result);

    }
}

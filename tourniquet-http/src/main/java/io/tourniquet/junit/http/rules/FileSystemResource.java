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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.util.ETag;
import io.undertow.util.MimeMappings;
import org.slf4j.Logger;

/**
 * A resource inside a {@link java.nio.file.FileSystem}. This resource uses the java.nio Files API which makes it
 * flexible for hosting files from various types of filesystems, such as ZipFileSystem. Created by Gerald Muecke on
 * 08.12.2015.
 */
public class FileSystemResource implements Resource {

    private static final Logger LOG = getLogger(FileSystemResource.class);

    private final Path path;

    /**
     * Creates a FileSystemResource for the specified Path.
     * @param path
     *  the path to the resource in the filesystem
     */
    public FileSystemResource(Path path) {
        if(path == null) {
            throw new IllegalArgumentException("Path must not be null");
        }
        this.path = path;
    }

    @Override
    public String getPath() {

        return path.toString();
    }

    @Override
    public Date getLastModified() {

        try {
            return new Date(Files.getLastModifiedTime(path).toMillis());
        } catch (IOException e) {
            throw new AssertionError("Could not determine last modified time", e);
        }
    }

    @Override
    public String getLastModifiedString() {

        try {
            return Files.getLastModifiedTime(path).toString();
        } catch (IOException e) {
            throw new AssertionError("Could not determine last modified time", e);
        }
    }

    @Override
    public ETag getETag() {

        return null;
    }

    @Override
    public String getName() {
        final Path filename = path.getFileName();
        if(filename == null){
            return null;
        }
        return filename.toString();

    }

    @Override
    public boolean isDirectory() {

        return Files.isDirectory(path);
    }

    @Override
    public List<Resource> list() {

        final List<Resource> result;
        if (Files.isDirectory(path)) {
            result = new ArrayList<>();
            try {
                for (Path child : Files.newDirectoryStream(path)) {
                    result.add(new FileSystemResource(child));
                }
            } catch (IOException e) {
                LOG.error("Could not read directory", e);
            }

        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    @Override
    public String getContentType(final MimeMappings mimeMappings) {
        final Path filenamePath = path.getFileName();
        if(filenamePath == null) {
            return null;
        }
        final String filename = filenamePath.toString();
        final int separator = filename.lastIndexOf('.');
        if (separator == -1) {
            return null;
        }
        return mimeMappings.getMimeType(filename.substring(separator + 1));
    }

    @Override
    public void serve(final Sender sender, final HttpServerExchange exchange, final IoCallback ioCallback) {

        exchange.startBlocking();
        final OutputStream outStream = exchange.getOutputStream();
        try {
            Files.copy(path, outStream);
            ioCallback.onComplete(exchange, sender);
        } catch (IOException e) {
            LOG.error("Could not serve content file", e);
            ioCallback.onException(exchange, sender, e);
        }
    }

    @Override
    public Long getContentLength() {

        try {
            return Files.size(path);
        } catch (IOException e) {
            throw new AssertionError("Could not determine content length", e);
        }
    }

    @Override
    public String getCacheKey() {

        return null;
    }

    @Override
    public File getFile() {

        return path.toFile();
    }

    @Override
    public Path getFilePath() {

        return path;
    }

    @Override
    public File getResourceManagerRoot() {
        final Path root = path.getRoot();
        if(root == null) {
            return null;
        }
        return root.toFile();
    }

    @Override
    public Path getResourceManagerRootPath() {

        return path.getRoot();
    }

    @Override
    public URL getUrl() {

        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new AssertionError("Could not create URL", e);
        }
    }
}

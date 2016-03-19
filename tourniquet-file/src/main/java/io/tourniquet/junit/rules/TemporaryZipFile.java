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

package io.tourniquet.junit.rules;

import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.Files.createDirectories;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.rules.TemporaryFolder;

/**
 * A zip file that is created for a test execution and deleted afterwards with content from resources.
 */
public class TemporaryZipFile extends TemporaryFile {

    private final Map<String, URL> contentMap;

    /**
     * Creates an ExternalFile in the specified temporary folder with the specified filename
     *
     * @param folder
     *         the temporary folder to create the file in
     * @param filename
     *         the name of the zip file
     * @param content
     *         the content for the zip file. The map contains a mapping for path names in the zip file to the URL
     *         containing the content for that entry
     */
    public TemporaryZipFile(final TemporaryFolder folder, final String filename, Map<String, URL> content) {

        super(folder, filename);
        this.contentMap = content;
    }

    @Override
    protected File createTempFile() throws IOException {

        final File file = newFile();
        try (FileSystem zipFs = newZipFileSystem(file)) {
            addEntries(zipFs);
        }
        return file;
    }

    /**
     * Adds the entries to the zip file, that have been defined by the builder.
     *
     * @param zipFs
     *         the zip file system that represents the new zip file.
     *
     * @throws IOException
     */
    private void addEntries(final FileSystem zipFs) throws IOException {

        for (Map.Entry<String, URL> entry : contentMap.entrySet()) {
            final Path pathToFile = zipFs.getPath(entry.getKey());
            final URL resource = entry.getValue();
            addResource(pathToFile, resource);
        }
    }

    /**
     * Creates a new zip file and exposes the zip file as a filesystem to which paths and files can be added.
     *
     * @param file
     *         the file handle that denotes the zip file
     *
     * @return the FileSystem that represents the zip file
     *
     * @throws IOException
     */
    private FileSystem newZipFileSystem(final File file) throws IOException {

        final Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        return newFileSystem(URI.create("jar:" + file.toURI()), env);
    }

    /**
     * Adds a resource respectively it's content to the filesystem at the position specified by the pathToFile
     *
     * @param pathToFile
     *         the path to the file or folder to which the resource's content should be written
     * @param resource
     *         the resource providing the content for the entry. If the resource is null, a folder will be created
     *
     * @throws IOException
     */
    private void addResource(final Path pathToFile, final URL resource) throws IOException {

        if (resource == null) {
            addFolder(pathToFile);
        } else {
            addEntry(pathToFile, resource);
        }
    }

    /**
     * Adds a folder entry for the specified path.
     *
     * @param pathToFile
     *         the path to the folder
     *
     * @throws IOException
     */
    private void addFolder(final Path pathToFile) throws IOException {

        createDirectories(pathToFile);
    }

    /**
     * Creates an entry under the specifeid path with the content from the provided resource.
     *
     * @param pathToFile
     *         the path to the file in the zip file.
     * @param resource
     *         the resource providing the content for the file. Must not be null.
     *
     * @throws IOException
     */
    private void addEntry(final Path pathToFile, final URL resource) throws IOException {

        final Path parent = pathToFile.getParent();
        if (parent != null) {
            addFolder(parent);
        }
        try (InputStream inputStream = resource.openStream()) {
            Files.copy(inputStream, pathToFile);
        }
    }

}

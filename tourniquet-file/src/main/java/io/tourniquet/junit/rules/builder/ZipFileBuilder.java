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

package io.tourniquet.junit.rules.builder;

import static io.tourniquet.junit.util.CallStack.getCallerClass;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.rules.TemporaryZipFile;
import io.tourniquet.junit.util.ResourceResolver;
import org.junit.rules.TemporaryFolder;

/**
 * A builder to build a temporary zip file from resources. Created by Gerald Muecke on 24.11.2015.
 */
public class ZipFileBuilder implements Builder<TemporaryFile> {

    private final TemporaryFolder folder;
    private final String filename;
    private final Map<String, URL> entryMap;
    private final ResourceResolver resolver;

    public ZipFileBuilder(final TemporaryFolder folder, String filename) {

        this.folder = folder;
        this.filename = filename;
        this.entryMap = new HashMap<>();
        this.resolver = new ResourceResolver(true);
    }

    @Override
    public TemporaryFile build() {

        return new TemporaryZipFile(folder, filename, entryMap);
    }

    /**
     * Adds an entry to the zip file from a classpath resource.
     *
     * @param zipEntryPath
     *         the path of the entry in the zip file. If the path denotes a path (ends with '/') the resource is put
     *         under its own name on that location. If it denotes a file, it will be put as this file into the zip. Note
     *         that even if the path ist defined absolute, starting with a '/', the created entry in the zip file won't
     *         start with a '/'
     * @param pathToResource
     *         the path to the resource in the classpath
     *
     * @return this builder
     */
    public ZipFileBuilder addClasspathResource(final String zipEntryPath, final String pathToResource) {

        final Class<?> callerClass = getCallerClass();
        final URL resource = resolver.resolve(pathToResource, callerClass);
        addResource(zipEntryPath, resource);
        return this;
    }

    /**
     * Adds a resource to the Zip File under the path specified.
     *
     * @param zipEntryPath
     *         the path to the entry in the zip file
     * @param resource
     *         the resource providing the content for the path. If an empty directory should be added, this value must
     *
     * @return this builder
     */
    public ZipFileBuilder addResource(String zipEntryPath, URL resource) {

        this.entryMap.put(zipEntryPath, resource);
        return this;
    }

    /**
     * Adds a folder entry to the zip file. Use this method if you require empty folders (including no subfolders) in
     * the zip file. <br> It is not necessary to added parent folders of a file to the zip as they are added implicitly
     * when adding a file.<br> * The folder remains empty unless additional entries with the same (parent) path as the
     * folder are added as well.
     *
     * @param pathToFolder
     *         path to the empty folder
     *
     * @return this builder
     */
    public ZipFileBuilder addFolder(final String pathToFolder) {

        addResource(pathToFolder, null);
        return this;
    }
}

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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * ResourceHandler that serves resources from a FileSystem {@link java.nio.file.Path}. The path may be of a physical
 * {@link java.nio.file.FileSystem} or a virtual one, such as a ZipFileSystem.
 */
public class PathResourceHandler extends ResourceHttpHandler {

    private final Path path;

    /**
     * Creates a resource handler for the specified path.
     * @param resourcePath
     *  the path to the resource in the filesystem.
     */
    public PathResourceHandler(final Path resourcePath) {
        this.path = resourcePath;
    }

    @Override
    protected void writeResource(final OutputStream outputStream) throws IOException {
            Files.copy(path, outputStream);
    }
}

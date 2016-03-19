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

import java.net.URL;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.util.ResourceResolver;
import io.tourniquet.junit.util.CallStack;
import org.junit.rules.TemporaryFolder;

/**
 * Builder for creating a temporary file in a temporary folder.
 *
 */
public class TemporaryFileBuilder implements Builder<TemporaryFile> {

    private final TemporaryFolder folder;
    private final String filename;
    private final ResourceResolver resolver;
    private URL content;
    private boolean forceContent;

    public TemporaryFileBuilder(final TemporaryFolder folder, final String fileName) {
        this.folder = folder;
        this.filename = fileName;
        this.resolver = new ResourceResolver(true);
    }

    @Override
    public TemporaryFile build() {

        final TemporaryFile file = new TemporaryFile(folder, filename);
        file.setForceContent(this.forceContent);
        file.setContentUrl(this.content);
        return file;
    }

    /**
     * Defines the classpath resource from where the content of the file should be retrieved
     * 
     * @param pathToResource
     *            the path to the classpath resource
     * @return the builder
     */
    public TemporaryFileBuilder fromClasspathResource(final String pathToResource) {
        final Class<?> callerClass = CallStack.getCallerClass();
        this.content = getResolver().resolve(pathToResource, callerClass);
        return this;
    }

    /**
     * Defines the resource by URL from where the content of the file should be retrieved. If the method {@link #asZip()}
     * is invoked after invoking this method, the content file will be added to the zip as element at root-level,
     * named exactly as the the resource file.
     *
     * @param resource
     *            the resource whose content will be used for the temporary file as content
     * @return the builder
     */
    public TemporaryFileBuilder fromResource(final URL resource) {
        this.content = resource;
        return this;
    }

    /**
     * Defines, that the external file must not be empty, which means, the rule enforces, the contentUrl is set. The
     * resource addressed by the URL may be empty nevertheless.
     * 
     * @return the builder
     */
    public TemporaryFileBuilder withContent() {
        this.forceContent = true;
        return this;
    }

    /**
     * Indicates the content for the file should be zipped. If only one content reference is provided, the zip
     * will only contain this file.
     * @return
     *  the builder
     */
    public ZipFileBuilder asZip() {
        final ZipFileBuilder zfb = new ZipFileBuilder(folder, filename);
        if(this.content != null) {
            zfb.addResource(getContenFileName(), this.content);
        }
        return zfb;
    }

    /**
     * Extracts the name of the resource from the url itself. The filename from the path-part of the URL is extracted.
     *
     * @return
     *  the name of the resource that provided the content
     */
    private String getContenFileName() {
        final String file  = this.content.getPath();
        if(file.indexOf('/') != -1){
            return file.substring(file.lastIndexOf('/'));
        }
        return file;
    }

    /**
     * The resource resolver helps locating resources in the classpath so that resources for building temporary
     * files can be declared more conveniently.
     * @return
     *  resource resolver for this builder.
     */
    protected ResourceResolver getResolver() {
        return resolver;
    }
}

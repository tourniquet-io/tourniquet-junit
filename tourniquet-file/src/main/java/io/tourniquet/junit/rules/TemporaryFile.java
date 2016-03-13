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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

import org.junit.rules.TemporaryFolder;

/**
 * A rule for creating an external file in a temporary folder with a specific content. If no content is defined an empty
 * file will be created
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class TemporaryFile extends ExternalResource<TemporaryFolder> {

    /**
     * The URL that points to the resource that provides the content for the file
     */
    private URL contentUrl;
    /**
     * The temporary folder the file will be created in
     */
    private final TemporaryFolder folder;
    /**
     * The name of the file
     */
    private final String filename;
    /**
     * The actual created file
     */
    private File file;
    /**
     * Flag to indicate that content URL must be set
     */
    private boolean forceContent;

    /**
     * Creates an ExternalFile in the specified temporary folder with the specified filename
     *
     * @param folder
     * @param filename
     */
    public TemporaryFile(final TemporaryFolder folder, final String filename) {

        super(folder);
        this.folder = folder;
        this.filename = filename;
    }

    @Override
    protected void before() throws Throwable {

        createTempFile();
    }

    /**
     * Creates a new empty file in the temporary folder. The file is the FS file on that is represented by this
     * TemporaryFile rule.
     * @return
     *  the file handle to the empty file
     * @throws IOException
     */
    protected File newFile() throws IOException {

        this.file = new File(folder.getRoot(), filename);
        return this.file;
    }

    /**
     * Creates the file including content. Override this method to implement a custom mechanism to create the temporary
     * file
     * @return
     *  the file handle to the newly created file
     * @throws IOException
     */
    protected File createTempFile() throws IOException {

        final File tempFile = newFile();
        if (forceContent && contentUrl == null) {
            throw new AssertionError("ContentUrl is not set");
        } else if (contentUrl == null) {
            createEmptyFile(tempFile);
        } else {
            try (InputStream inputStream = contentUrl.openStream()) {
                Files.copy(inputStream, tempFile.toPath());
            }
        }
        return tempFile;
    }

    private void createEmptyFile(final File tempFile) throws IOException {
        if(!tempFile.createNewFile()){
            throw new AssertionError("Could not create temp file " + tempFile);
        }
    }

    @Override
    protected void after() {

        file.delete(); // NOSONAR

    }

    @Override
    protected void beforeClass() throws Throwable {

        before();
    }

    @Override
    protected void afterClass() {

        after();
    }

    /**
     * Returns the file handle of the external file
     *
     * @return
     */
    public File getFile() {

        return file;
    }

    /**
     * Sets the URL that contains the content for the file. <br>
     * The method must be invoked before the rule is applied.
     *
     * @param contentUrl
     */
    @RuleSetup
    public void setContentUrl(final URL contentUrl) {

        this.contentUrl = contentUrl;
    }

    /**
     * Setting this to true will ensure, the file has content provided by the content url. If set to false the file may
     * not have a content url associated and therefore may be empty. <br>
     * <br>
     * The method must be invoked before the rule is applied.
     *
     * @param forceContent
     *            <code>true</code> if contentURL has to be set
     */
    @RuleSetup
    public void setForceContent(final boolean forceContent) {

        this.forceContent = forceContent;
    }

}

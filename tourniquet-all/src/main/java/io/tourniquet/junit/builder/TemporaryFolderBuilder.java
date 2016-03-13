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

package io.tourniquet.junit.builder;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.jcr.rules.StandaloneContentRepository;
import io.tourniquet.junit.jcr.rules.builder.InMemoryContentRepositoryBuilder;
import io.tourniquet.junit.jcr.rules.builder.StandaloneContentRepositoryBuilder;
import io.tourniquet.junit.rules.TemporaryFile;
import io.tourniquet.junit.rules.builder.TemporaryFileBuilder;
import io.tourniquet.junit.rules.ldap.builder.DirectoryBuilder;
import org.junit.rules.TemporaryFolder;

/**
 * Builder for the Temporary Folder.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class TemporaryFolderBuilder implements Builder<TemporaryFolder> {

    private final TemporaryFolder temporaryFolder;

    public TemporaryFolderBuilder() {
        temporaryFolder = new TemporaryFolder();
    }

    /**
     * Creates a builder for an {@link StandaloneContentRepository} that is chained inside the {@link TemporaryFolder}
     *
     * @return an {@link io.tourniquet.junit.jcr.rules.builder.StandaloneContentRepositoryBuilder}
     */
    public StandaloneContentRepositoryBuilder aroundStandaloneContentRepository() {
        return new StandaloneContentRepositoryBuilder(build());
    }

    @Override
    public TemporaryFolder build() {

        return temporaryFolder;
    }

    /**
     * Creates a builder for an {@link io.tourniquet.junit.jcr.rules.InMemoryContentRepository} that is chained inside the {@link TemporaryFolder}
     *
     * @return an {@link InMemoryContentRepositoryBuilder}
     */
    public InMemoryContentRepositoryBuilder aroundInMemoryContentRepository() {
        return new InMemoryContentRepositoryBuilder(build());
    }

    public DirectoryBuilder aroundDirectory() {

        return new DirectoryBuilder(temporaryFolder);
    }

    /**
     * Creates a builder for a {@link TemporaryFile} that is chained inside the {@link TemporaryFolder}
     *
     * @param filename
     *            the name of the temporary file
     * @return a {@link TemporaryFileBuilder}
     */
    public TemporaryFileBuilder aroundTempFile(final String filename) {
        return new TemporaryFileBuilder(build(), filename);
    }
}

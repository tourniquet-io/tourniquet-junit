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

package io.tourniquet.junit.rules.ldap.builder;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.tourniquet.junit.Builder;
import io.tourniquet.junit.rules.ldap.Directory;
import org.junit.rules.TemporaryFolder;

/**
 * Builder for creating a {@link Directory} rule. The builder allows to add partitions, initial content and basic
 * settings so that the entire rule can be set up with chained method calls.
 */
public class DirectoryBuilder implements Builder<Directory> {

    private transient final TemporaryFolder temporaryFolder;

    private transient final Map<String, String> partitions;

    private transient URL ldif;

    /**
     * Flag that indicates that access control should be enabled in the directory service.
     */
    private transient boolean acEnabled;

    /**
     * Flag that indicates that anonymous access should be allowed.
     */
    private transient boolean anonymousAllowed = true;

    /**
     * Constructor accepting a temporary folder. The directory requires a working directory where the contents of the
     * directory or temporary files are put in.
     *
     * @param temporaryFolder
     *         the temporary folder rule that is used to create a working directory
     */
    public DirectoryBuilder(final TemporaryFolder temporaryFolder) {

        this.temporaryFolder = temporaryFolder;
        this.partitions = new HashMap<>();
    }

    /**
     * Wraps the {@link Directory} rule around a directory server that makes the service accessible through the LDAP
     * protocol via TCP connection.
     *
     * @return a {@link DirectoryServerBuilder} that can be used to create a server using the service as content
     *      provider
     */
    public DirectoryServerBuilder aroundDirectoryServer() {

        return new DirectoryServerBuilder(this.build());
    }

    @Override
    public Directory build() {

        final Directory dir = new Directory(this.temporaryFolder);
        for (Map.Entry<String, String> partitionEntry : this.partitions.entrySet()) {

            try {
                dir.addPartition(partitionEntry.getKey(), partitionEntry.getValue());
            } catch (Exception e) { //NOSONAR
                throw new AssertionError("Could not add partition "
                                                 + partitionEntry.getKey()
                                                 + ", "
                                                 + partitionEntry.getValue(), e);
            }
        }
        if (this.ldif != null) {
            dir.setInitialContentLdif(this.ldif);
        }
        dir.setAcEnabled(this.acEnabled);
        dir.setAnonymousAccess(this.anonymousAllowed);

        return dir;
    }

    /**
     * Adds a new partition to the {@link Directory} on initialization.
     *
     * @param partitionId
     *         the id of the partition
     * @param suffix
     *         the suffix of the parition so that it can be addressed using a DN
     *
     * @return this builder
     */
    public DirectoryBuilder withPartition(final String partitionId, final String suffix) {

        this.partitions.put(partitionId, suffix);
        return this;
    }

    /**
     * Specifies an LDIF file that is imported into the {@link Directory} on initialization.
     *
     * @param ldif
     *         the location of the ldif to import
     *
     * @return this builder
     */
    public DirectoryBuilder importLdif(final URL ldif) {

        this.ldif = ldif;

        return this;
    }

    /**
     * Enables access control on the {@link Directory}. Default setting is disabled access control.
     *
     * @return this builder
     */
    public DirectoryBuilder accessControlEnabled() {

        this.acEnabled = true;
        return this;
    }

    /**
     * Disables anonymous access on the {@link Directory}. Default setting is anonymous access allowed.
     *
     * @return this builder
     */
    public DirectoryBuilder anonymousAccessDisabled() {

        this.anonymousAllowed = false;
        return this;
    }
}

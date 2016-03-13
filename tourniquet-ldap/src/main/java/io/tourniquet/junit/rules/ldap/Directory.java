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

package io.tourniquet.junit.rules.ldap;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.tourniquet.junit.rules.ExternalResource;
import io.tourniquet.junit.rules.RuleSetup;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.CoreSession;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.InstanceLayout;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.factory.DefaultDirectoryServiceFactory;
import org.apache.directory.server.core.factory.DirectoryServiceFactory;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.protocol.shared.store.LdifFileLoader;
import org.apache.directory.server.xdbm.impl.avl.AvlIndex;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;

/**
 * Test Rule that provides a directory service. If you need an LDAP directory server, you have to embed this rule in the
 * {@link DirectoryServer} rule.
 *
 */
public class Directory extends ExternalResource<TemporaryFolder> {

    private static final Logger LOG = getLogger(Directory.class);


    /**
     * Map of partitions that should be created on directory initialization.
     */
    private transient final Map<String, String> partitions;

    /**
     * The directory services provided and managed by this rule.
     */
    private transient DirectoryService directoryService;

    /**
     * Access Control Enabled flag.
     */
    private transient boolean acEnabled;

    /**
     * Anonymous Access Allowed.
     */
    private transient boolean anonymousAllowed = true;

    /**
     * The working directory for the directory service.
     */
    private transient File workDir;

    /**
     * URL of the ldif file that should be imported on initialization of the directory.
     */
    private transient URL initialLdif;

    public Directory(final TemporaryFolder folder) {

        super(folder);
        this.partitions = new HashMap<>();
    }

    @Override
    protected void beforeClass() throws Throwable {

        before();
    }

    @Override
    protected void afterClass() {

        after();
    }

    @Override
    protected void before() throws Throwable {

        setupService();
        startService();
    }

    @Override
    protected void after() {

        try {
            tearDownService();
        } catch (Exception e) {
            throw new AssertionError("Error tearing down the directoy", e);
        }
    }

    /**
     * Shuts down the directory service.
     *
     * @throws Exception
     *         if the shutdown fails for any reason
     */
    protected void tearDownService() throws Exception { // NOSONAR

        this.getDirectoryService().shutdown();

    }

    /**
     * The Apache DS Directory Service instance wrapped by this rule.
     *
     * @return the {@link org.apache.directory.server.core.api.DirectoryService} instance of this rule
     */
    public DirectoryService getDirectoryService() {

        if (this.directoryService == null) {
            this.directoryService = this.createDirectoryService();
        }

        return this.directoryService;
    }

    /**
     * Creates a new DirectoryService instance for the test rule. Initialization of the service is done in the
     * apply Statement phase by invoking the setupService method.
     */
    private DirectoryService createDirectoryService() {

        final DirectoryServiceFactory factory = new DefaultDirectoryServiceFactory();
        try {
            factory.init("tourniquet");
            return factory.getDirectoryService();
        } catch (Exception e) { //NOSONAR

            throw new AssertionError("Unable to create directory service", e);
        }
    }

    /**
     * Applies the configuration to the service such as AccessControl and AnonymousAccess. Both are enabled as
     * configured. Further, the method initializes the cache service. The method does not start the service.
     *
     * @throws Exception
     *         if starting the directory service failed for any reason
     */
    protected void setupService() throws Exception { // NOSONAR

        final DirectoryService service = this.getDirectoryService();
        service.getChangeLog().setEnabled(false);

        this.workDir = getOuterRule().newFolder("dsworkdir");

        service.setInstanceLayout(new InstanceLayout(this.workDir));
        final CacheService cacheService = new CacheService();
        cacheService.initialize(service.getInstanceLayout());
        service.setCacheService(cacheService);

        service.setAccessControlEnabled(this.acEnabled);
        service.setAllowAnonymousAccess(this.anonymousAllowed);

        this.createPartitions();
        this.importInitialLdif();
    }

    /**
     * Starts the service.
     *
     * @throws Exception
     *         if the service could not be started for any reason
     */
    protected void startService() throws Exception { //NOSONAR

        this.getDirectoryService().startup();
    }

    /**
     * Initializes the directory with content from the initial ldif file. Note that a partition has to be created for
     * the root of the ldif file.
     *
     * @throws IOException
     *         if the resource pointing to the ldif file to be imported can not be accessed
     */
    private void importInitialLdif() throws IOException {

        if (this.initialLdif != null) {
            try (InputStream ldifStream = this.initialLdif.openStream()) {
                this.importLdif(ldifStream);
            }
        }
    }

    /**
     * Creates all paritions that are added on rule setup.
     *
     */
    private void createPartitions() {

        for (Map.Entry<String, String> partitionEntry : this.partitions.entrySet()) {
            try {
                this.addPartitionInternal(partitionEntry.getKey(), partitionEntry.getValue());
            } catch (Exception e) { //NOSONAR
                throw new AssertionError("Could not create partitions " + this.partitions, e);
            }
        }
    }

    /**
     * Creates an AVL implementation based in-memory partition. A partition is required to add entries or import LIDF
     * data. Once the partition was added, the context entry has to be created. If you're using the ldif import, the use
     * of this method and the ldif file may look like
     * <pre>
     *     <code>
     *      directory.addPartition(&quot;tourniquet&quot;, &quot;dc=tourniquet&quot);
     *      ...
     *      // LDIF File
     *      dn: dc=tourniquet
     *      objectClass: top
     *      objectClass: dcObject
     *      objectClass: organization
     *      o: tourniquet.io
     *     </code>
     * </pre>
     * The method may be invoked after the service is started, i.e. in the setUp method of a test.
     *
     * @param partitionId
     *         the id of the partition
     * @param suffix
     *         the suffix dn of all partition entries
     *
     * @throws Exception
     *         if the partition could not be created
     */
    protected void addPartitionInternal(final String partitionId, final String suffix) throws Exception { //NOSONAR

        final DirectoryService service = this.getDirectoryService();

        final CacheService cacheService = service.getCacheService();
        final SchemaManager schemaManager = service.getSchemaManager();
        final DnFactory dnFactory = service.getDnFactory();

        final URI partitionPath = new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI();
        final AvlPartition partition = new AvlPartition(schemaManager, dnFactory);
        partition.setId(partitionId);
        partition.setSuffixDn(dnFactory.create(suffix));
        partition.setCacheService(cacheService);
        partition.setCacheSize(1000);
        partition.setSyncOnWrite(true);
        partition.setPartitionPath(partitionPath);
        partition.addIndex(new AvlIndex<Entry>("objectClass", false));
        partition.initialize();
        LOG.info("Created partition {} in {}", partitionId, partitionPath);
        service.addPartition(partition);
    }

    /**
     * Adds a partition to the rule. Partitions can only be added before the rule is applied and the service is started
     * up. So this method is intended to be invoked by a builder but may be used by a test as well. The test have to
     * create the partition instance itself allowing to use different partition implementations.
     *
     * @param partition
     *         the partition to be added to the service
     *
     * @throws Exception
     *         if adding the partition failed for any reason
     */
    protected void addPartitionInternal(Partition partition) throws Exception { //NOSONAR

        this.getDirectoryService().addPartition(partition);
    }

    /**
     * Specifies the location of an ldif file that is imported on initialization of the rule.
     *
     * @param ldif
     *         the url to the ldif file
     */
    @RuleSetup
    public void setInitialContentLdif(URL ldif) {

        this.initialLdif = ldif;
    }

    /**
     * Imports directory content that is defined in LDIF format and provided as input stream. The method writes the
     * stream content into a temporary file.
     *
     * @param ldifData
     *         the ldif data to import as a stream
     *
     * @throws IOException
     *         if the temporary file can not be created
     */
    public void importLdif(InputStream ldifData) throws IOException {

        final File ldifFile = getOuterRule().newFile("tourniquet_import.ldif");
        try (final Writer writer = new OutputStreamWriter(new FileOutputStream(ldifFile), Charsets.UTF_8)) {

            IOUtils.copy(ldifData, writer);
        }
        final String pathToLdifFile = ldifFile.getAbsolutePath();
        final CoreSession session = this.getDirectoryService().getAdminSession();
        final LdifFileLoader loader = new LdifFileLoader(session, pathToLdifFile);
        loader.execute();

    }

    /**
     * Enables access control on the directory service. Default is false.
     *
     * @param acEnabled
     *         flag to indicate, if access control should be enabled
     */
    @RuleSetup
    public void setAcEnabled(final boolean acEnabled) {

        this.acEnabled = acEnabled;
    }

    /**
     * Enables anonymous access on the directory service. Default is true.
     *
     * @param anonymousAccess
     *         <code>true</code> to enable anoynmous access (default) and <code>false</code> to disable anonymous
     *         access
     */
    @RuleSetup
    public void setAnonymousAccess(final boolean anonymousAccess) {

        this.anonymousAllowed = anonymousAccess;
    }

    /**
     * Adds a partition to the rule. The actual parititon is created when the rule is applied.
     *
     * @param partitionId
     *         the id of the partition
     * @param suffix
     *         the suffix of the partition
     */
    @RuleSetup
    public void addPartition(String partitionId, String suffix) {

        this.partitions.put(partitionId, suffix);
    }

}

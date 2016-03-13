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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.net.URL;
import java.util.Set;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.schema.SchemaManager;
import org.apache.directory.server.core.api.CacheService;
import org.apache.directory.server.core.api.DirectoryService;
import org.apache.directory.server.core.api.DnFactory;
import org.apache.directory.server.core.api.partition.Partition;
import org.apache.directory.server.core.partition.impl.avl.AvlPartition;
import org.apache.directory.server.xdbm.impl.avl.AvlIndex;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryTest {

    private static final Logger LOG = getLogger(DirectoryTest.class);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    /**
     * The class under test
     */
    private Directory subject;
    @Mock
    private Description description;

    @Before
    public void setUp() throws Exception {

        LOG.info("Creating Directory Rule");
        subject = new Directory(folder);
    }

    @After
    public void tearDown() throws Exception {
        DirectoryService service = subject.getDirectoryService();
        if(service != null && service.isStarted()) {
            service.shutdown();
        }
    }

    @Test
    public void testApply() throws Throwable {
        //prepare
        Statement base = spy(new Statement() {

            @Override
            public void evaluate() throws Throwable {
                assertTrue(subject.getDirectoryService().isStarted());
            }
        });

        //act
        Statement stmt = subject.apply(base, description);

        //assert
        assertNotNull(stmt);
        //run the created statement, causing the evaluate method to be executed, which is verified with the next
        //statement. If the service is not started before the evaluate method, the evaluate method will fail
        stmt.evaluate();
        verify(base).evaluate();
        //verify, the service is stopped after evaluate
        assertFalse(subject.getDirectoryService().isStarted());
    }

    @Test
    public void testApplyAsClassRule() throws Throwable {
        //prepare
        when(description.isSuite()).thenReturn(true);
        Statement base = spy(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertTrue(subject.getDirectoryService().isStarted());
            }
        });

        //act
        Statement stmt = subject.apply(base, description);

        //assert
        assertNotNull(stmt);
        //run the created statement, causing the evaluate method to be executed, which is verified with the next
        //statement. If the service is not started before the evaluate method, the evaluate method will fail
        stmt.evaluate();
        verify(base).evaluate();
        //verify, the service is stopped after evaluate
        assertFalse(subject.getDirectoryService().isStarted());
    }

    @Test
    public void testSetupService_withAccessControl() throws Exception {

        LOG.info("ENTER testSetupService_withAccessControl ()");

        //prepare
        this.subject.setAcEnabled(true);
        this.subject.setAnonymousAccess(false);
        //act
        subject.setupService();

        //assert
        DirectoryService service = subject.getDirectoryService();
        assertNotNull(service);

        assertTrue(service.isAccessControlEnabled());
        assertFalse(service.isAllowAnonymousAccess());
        assertNotNull(service.getCacheService());
    }

    @Test
    public void testSetupService_withAnonymousAccesss() throws Exception {

        LOG.info("ENTER testSetupService_withAnonymousAccesss ()");
        //prepare
        this.subject.setAcEnabled(false);
        this.subject.setAnonymousAccess(true);
        //act
        subject.setupService();

        //assert
        DirectoryService service = subject.getDirectoryService();
        assertNotNull(service);

        assertFalse(service.isAccessControlEnabled());
        assertTrue(service.isAllowAnonymousAccess());
        assertNotNull(service.getCacheService());
    }

    @Test
    public void testAddPartition_Partition() throws Exception {
        //prepare
        String partitionId = "testPartition";
        String partitionSuffix = "dc=test";
        final DirectoryService service = subject.getDirectoryService();

        final CacheService cacheService = service.getCacheService();
        final SchemaManager schemaManager = service.getSchemaManager();
        final DnFactory dnFactory = service.getDnFactory();

        final AvlPartition partition = new AvlPartition(schemaManager, dnFactory);
        partition.setId(partitionId);
        partition.setSuffixDn(dnFactory.create(partitionSuffix));
        partition.setCacheService(cacheService);
        partition.setSyncOnWrite(true);
        partition.setCacheSize(1000);
        partition.setPartitionPath(new File(service.getInstanceLayout().getPartitionsDirectory(), partitionId).toURI());
        partition.addIndex(new AvlIndex<Entry>("objectClass", false));
        partition.initialize();

        //act
        subject.addPartitionInternal(partition);
        subject.setupService();

        //assert
        Set<? extends Partition> partitions = subject.getDirectoryService().getPartitions();
        assertContainsPartition(partitions, partitionId, partitionSuffix);
    }

    private void assertContainsPartition(final Set<? extends Partition> partitions,
                                         final String partitionId,
                                         final String partitionSuffix) {

        Partition actual = null;
        for (Partition p : partitions) {
            if (partitionId.equals(p.getId())) {
                actual = p;
                break;
            }
        }
        assertNotNull(partitionId + " not found", actual);
        assertEquals(partitionSuffix, actual.getSuffixDn().toString());
    }

    @Test
    public void testAddPartition_IdAndSuffix() throws Exception {
         //prepare

        //act
        subject.addPartition("testPartition", "dc=test");
        subject.setupService();

        //assert

        Set<? extends Partition> partitions = subject.getDirectoryService().getPartitions();
        assertContainsPartition(partitions, "testPartition", "dc=test");
    }

    @Test
    public void testImportLdif() throws Exception {
        //prepare
        URL ldifResource = getClass().getResource("DirectoryTest_testUsers.ldif");
        subject.setupService();
        subject.startService();
        subject.addPartitionInternal("tourniquet", "dc=tourniquet");

        //act
        subject.importLdif(ldifResource.openStream());

        //assert
        assertTrue(subject.getDirectoryService().getSession().exists("uid=testuser,ou=users,dc=tourniquet"));
    }

    @Test
    public void testTearDownService() throws Exception {
        //prepare
        subject.setupService();

        //act
        subject.tearDownService();

        //assert
        assertFalse(subject.getDirectoryService().isStarted());
    }
}

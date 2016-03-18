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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.directory.server.core.api.CoreSession;
import org.apache.directory.server.core.api.partition.Partition;
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

import io.tourniquet.junit.rules.ldap.Directory;

@RunWith(MockitoJUnitRunner.class)
public class DirectoryBuilderTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private Description description;

    private DirectoryBuilder subject;
    private Directory dir;

    @Before
    public void setUp() throws Exception {
        this.subject = new DirectoryBuilder(folder);
    }

    @After
    public void tearDown() throws Exception {

        if (dir != null) {
            dir.getDirectoryService().shutdown();
        }
    }

    @Test
    public void testAroundDirectoryServer() throws Exception {

        //prepare

        //act
        DirectoryServerBuilder dsb = subject.aroundDirectoryServer();

        //assert
        assertNotNull(dsb);

    }

    @Test
    public void testWithPartition() throws Throwable {
        //prepare
        final String partitionId = "test";
        String suffix = "ou=test";

        //act
        DirectoryBuilder builder = subject.withPartition(partitionId, suffix);

        //assert
        assertNotNull(builder);
        assertSame(builder, subject);
        this.dir = subject.build();
        this.dir.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertFalse(dir.getDirectoryService().isAccessControlEnabled());
                assertTrue(dir.getDirectoryService().isAllowAnonymousAccess());
                final Map<String, Partition> partitionIds = getPartitions(dir);
                assertTrue(partitionIds.containsKey(partitionId));
            }
        }, description).evaluate();
    }

    private Map<String, Partition> getPartitions(final Directory dir) {

        Set<? extends Partition> partitions = dir.getDirectoryService().getPartitions();
        assertNotNull(partitions);
        Map<String, Partition> partitionIds = new HashMap<>();
        for (Partition p : partitions) {
            partitionIds.put(p.getId(), p);
        }
        return partitionIds;
    }

    @Test
    public void testImportLdif() throws Throwable {
        //prepare
        URL ldif = getClass().getResource("DirectoryBuilderTest_testUsers.ldif");

        //act
        DirectoryBuilder builder = subject.withPartition("tourniquet", "dc=tourniquet").importLdif(ldif);

        //assert
        assertNotNull(builder);

        dir = builder.build();
        dir.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertFalse(dir.getDirectoryService().isAccessControlEnabled());
                assertTrue(dir.getDirectoryService().isAllowAnonymousAccess());
                dir.getDirectoryService().setAllowAnonymousAccess(true);
                CoreSession session = dir.getDirectoryService().getSession();
                assertTrue(session.exists("uid=testuser,ou=users,dc=tourniquet"));

            }
        }, description).evaluate();
    }


    @Test
    public void testAccessControlEnabled() throws Throwable {
        //prepare

        //act
        DirectoryBuilder builder = subject.accessControlEnabled();

        //assert
        assertSame(builder, subject);
        assertNotNull(builder);
        assertSame(builder, subject);

        dir = builder.build();
        dir.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertTrue(dir.getDirectoryService().isAllowAnonymousAccess());
                assertTrue(dir.getDirectoryService().isAccessControlEnabled());
            }
        }, description).evaluate();

    }

    @Test
    public void testAnonymousAccessDisabled() throws Throwable {
        //prepare

        //act
        DirectoryBuilder builder = subject.anonymousAccessDisabled();

        //assert
        assertSame(builder, subject);
        assertNotNull(builder);
        assertSame(builder, subject);

        dir = builder.build();
        dir.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                assertFalse(dir.getDirectoryService().isAllowAnonymousAccess());
                assertFalse(dir.getDirectoryService().isAccessControlEnabled());
            }
        }, description).evaluate();

    }

    @Test
    public void testBuild() throws Throwable {

        //prepare

        //act
        //need to create the partition, but we create no entries in it
        this.dir = subject.withPartition("tourniquet", "dc=tourniquet").build();

        //assert
        assertNotNull(dir);

        //validate default settings
        dir.apply(new Statement() {

            @Override
            public void evaluate() throws Throwable {

                //access control is disabled
                assertFalse(dir.getDirectoryService().isAccessControlEnabled());
                //anonymous access is allowed
                assertTrue(dir.getDirectoryService().isAllowAnonymousAccess());
                dir.getDirectoryService().setAllowAnonymousAccess(true);
                CoreSession session = dir.getDirectoryService().getSession();
                //no ldif import of any users took place
                assertFalse(session.exists("dc=tourniquet"));

            }
        }, description).evaluate();


    }
}

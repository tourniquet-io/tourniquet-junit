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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.FileSystem;
import java.nio.file.Path;

import io.undertow.server.handlers.resource.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileSystemResourceManagerTest {

    /**
     * The class under test
     */
    @InjectMocks
    private FileSystemResourceManager subject;

    @Mock
    private FileSystem fileSystem;

    @Mock
    private Path path;

    @Test
    public void testGetResource() throws Exception {
        //prepare
        when(path.getFileName()).thenReturn(path);
        when(path.toString()).thenReturn("TEST");
        when(fileSystem.getPath("test")).thenReturn(path);

        //act
        Resource res = subject.getResource("test");

        //assert
        assertNotNull(res);
        assertEquals("TEST", res.getName());
    }

    @Test
    public void testIsResourceChangeListenerSupported() throws Exception {

        //prepare

        //act
        assertFalse(subject.isResourceChangeListenerSupported());

        //assert
    }

    @Test
    public void testClose() throws Exception {

        //prepare

        //act
        subject.close();

        //assert
        verify(fileSystem).close();
    }
}

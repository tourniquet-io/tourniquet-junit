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

package io.tourniquet.junit.jcr.rules;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.jcr.Repository;
import javax.naming.Context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JNDIContentRepositoryTest {

    @Mock
    private Context context;
    @Mock
    private Repository repository;

    private JNDIContentRepository subject;

    @Before
    public void setUp() throws Exception {
        subject = new JNDIContentRepository();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = AssertionError.class)
    public void testCreateRepository_noContextNoLookup() throws Exception {
        subject.createRepository();
    }

    @Test
    public void testCreateRepository_withContextNoLookup_defaultLookup() throws Exception {
        // prepare
        subject.setContext(context);
        when(context.lookup("java:/rules/local")).thenReturn(repository);
        // act
        final Repository jndiRepo = subject.createRepository();

        // assert
        assertNotNull(jndiRepo);
        assertEquals(repository, jndiRepo);
    }

    @Test(expected = AssertionError.class)
    public void testCreateRepository_noContextWithLookup() throws Exception {
        subject.setLookupName("java:/rules/local");
        subject.createRepository();
    }

    @Test
    public void testCreateRepository_withContextAndWithLookup() throws Exception {
        // prepare
        subject.setContext(context);
        subject.setLookupName("java:/custom/lookup/name");
        when(context.lookup("java:/custom/lookup/name")).thenReturn(repository);
        // act
        final Repository jndiRepo = subject.createRepository();

        // assert
        assertNotNull(jndiRepo);
        assertEquals(repository, jndiRepo);
    }

}

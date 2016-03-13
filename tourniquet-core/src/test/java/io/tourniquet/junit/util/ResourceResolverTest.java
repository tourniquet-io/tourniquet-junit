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

package io.tourniquet.junit.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URL;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 25.11.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceResolverTest{

    private ResourceResolver subject = new ResourceResolver();
    private ClassLoader origCtxCl;

    @Before
    public void setUp() throws Exception {
        this.origCtxCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(null);
    }

    @After
    public void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(origCtxCl);
    }

    @Test
    public void resolve_noContextCL_absolute_no_hint() throws Exception {
        //prepare

        //act
        URL res = subject.resolve("/resolver/resolverResource.txt");

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_withContextCL_absolute_no_hint() throws Exception {
        //prepare
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        //act
        URL res = subject.resolve("/resolver/resolverResource.txt");

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_noContextCL_absolute_withClass() throws Exception {
        //prepare

        //act
        URL res = subject.resolve("/resolver/resolverResource.txt", getClass());

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_withContextCL_absolute_withClass() throws Exception {
        //prepare
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        //act
        URL res = subject.resolve("/resolver/resolverResource.txt", getClass());

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_noContextCL_relative_samePackage_withClass() throws Exception {
        //prepare

        //act
        URL res = subject.resolve("resolverResource.txt", getClass());

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_withContextCL_relative_samePackage_WithClass() throws Exception {
        //prepare
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        //act
        URL res = subject.resolve("resolverResource.txt", getClass());

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_noContextCL_relative_relativePackageToClass() throws Exception {
        //prepare

        //act
        URL res = subject.resolve("relative/relativeResource.txt", getClass());

        //assert
        assertNotNull(res);
    }

    @Test
    public void resolve_withContextCL_relative_relativePackageToClass() throws Exception {
        //prepare
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        //act
        URL res = subject.resolve("relative/relativeResource.txt", getClass());

        //assert
        assertNotNull(res);
    }


    @Test
    public void testResolve_notFailOnMissing_and_resourceMissing_ok() throws Exception {
        //prepare
        ResourceResolver resolver = new ResourceResolver(false);
        //act
        URL resource  = resolver.resolve("nonExistingResource");
        //assert
        assertNull(resource);

    }

    @Test(expected = AssertionError.class)
    public void testResolve_failOnMissing_and_resourceMissing_failure() throws Exception {

        //prepare
        ResourceResolver resolver = new ResourceResolver(true);
        //act
        resolver.resolve("nonExistingResource");
        //assert
        //should fail
    }


}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

/**
 *
 */
public class TestClassLoaderTest {

    private URL testJar = new ResourceResolver(true).resolve("test1.zip");

    @Test
    public void testLoadClass_testClass() throws Exception {

        //prepare
        TestClassLoader cl = new TestClassLoader(Arrays.asList(testJar));

        //act
        Class cls1 = cl.loadClass("io.devcon5.classutils.ClassStreams");
        Class cls2 = cl.loadClass("io.devcon5.classutils.ClassStreams");

        //assert
        assertEquals(cl, cls1.getClassLoader());
        assertEquals(cls1.getClassLoader(), cls2.getClassLoader());
        assertSame(cls1, cls2);
    }

    @Test
    public void testLoadClass_nonTestClass() throws Exception {

        //prepare
        TestClassLoader cl = new TestClassLoader(Collections.singletonList(testJar));

        //act
        Class cls1 = cl.loadClass(TestClassLoaderTest.class.getName());
        Class cls2 = cl.loadClass(TestClassLoaderTest.class.getName());

        //assert
        assertEquals(cls1.getClassLoader(), cl);
        assertSame(cls1, cls2);
    }
}

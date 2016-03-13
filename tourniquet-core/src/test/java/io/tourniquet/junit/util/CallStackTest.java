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
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 26.11.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class CallStackTest {

    @Mock
    private ClassLoader classLoader;
    private ClassLoader origCtxCl;

    @Before
    public void setUp() throws Exception {
        this.origCtxCl = Thread.currentThread().getContextClassLoader();
    }

    @After
    public void tearDown() throws Exception {
        Thread.currentThread().setContextClassLoader(origCtxCl);
    }

    @Test
    public void testGetCallerClass_defaultClassLoader() throws Exception {

        //prepare

        //act
        Class<?> cls = new CallingClass().callingMethod();

        //assert
        assertEquals(CallStackTest.class, cls);
    }


    @Test
    public void testGetCallerClass_contextClassLoader() throws Exception {

        //prepare
        when(classLoader.loadClass(CallStackTest.class.getName())).thenReturn((Class)MockClass.class);
        Thread.currentThread().setContextClassLoader(classLoader);

        //act
        Class<?> cls = new CallingClass().callingMethod();

        //assert
        assertEquals(MockClass.class, cls);
    }

    /**
     * Placeholder for a class that wants to get the caller of the calling method.
     */
    public static class CallingClass {
        private Class<?> callingMethod() {

            return CallStack.getCallerClass();
        }
    }

    public static class MockClass extends CallStackTest {

    }
}

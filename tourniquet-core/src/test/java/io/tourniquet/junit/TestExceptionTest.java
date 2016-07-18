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

package io.tourniquet.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 *
 */
public class TestExceptionTest {

    @Test
    public void testDefaultConstructor() throws Exception {
        //prepare

        //act
        TestException x = new TestException();

        //assert
        assertNotNull(x.getMessage());
        assertEquals("TestException", x.getMessage());

    }

    @Test
    public void testExceptionWithMessage() throws Exception {
        //prepare

        //act
        TestException x = new TestException("message");

        //assert
        assertEquals("message", x.getMessage());

    }

    @Test
    public void testExceptionWithThrowable() throws Exception {
        //prepare
        RuntimeException rx = new RuntimeException();

        //act
        TestException x = new TestException(rx);

        //assert
        assertNotNull(x.getMessage());
        assertEquals(rx, x.getCause());

    }

    @Test
    public void testExceptionWithTrowableAndMessage() throws Exception {
        //prepare
        RuntimeException rx = new RuntimeException();

        //act
        TestException x = new TestException("message", rx);

        //assert
        assertEquals("message", x.getMessage());
        assertEquals(rx, x.getCause());

    }

}

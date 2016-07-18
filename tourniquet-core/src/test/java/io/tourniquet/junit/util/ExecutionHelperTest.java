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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import io.tourniquet.junit.UncheckedException;
import org.junit.Test;

/**
 *
 */
public class ExecutionHelperTest {

    @Test
    public void testRunUnchecked_runnable_noException() throws Exception {

        //prepare
        AtomicBoolean run = new AtomicBoolean(false);

        //act
        ExecutionHelper.runUnchecked(() -> run.set(true));

        //assert
        assertTrue(run.get());
    }

    @Test(expected = UncheckedException.class)
    public void testRunUnchecked_runnable_withException() throws Exception {
        //act
        ExecutionHelper.runUnchecked((Runnable) () -> {
            throw new IllegalArgumentException();
        });
    }

    @Test
    public void testRunUnchecked_callable_noException() throws Exception {

        //prepare
        Object expected = new Object();

        //act
        Object actual = ExecutionHelper.runUnchecked(() -> expected);

        //assert
        assertEquals(expected, actual);
    }

    @Test(expected = UncheckedException.class)
    public void testRunUnchecked_callable_withException() throws Exception {
        //act
        ExecutionHelper.runUnchecked((Callable) () -> {
            throw new IllegalArgumentException();
        });
    }

    @Test
    public void testRunProtected_runnable() throws Exception {
        //prepare
        AtomicBoolean run = new AtomicBoolean(false);

        //act
        ExecutionResult<Void> result = ExecutionHelper.runProtected(() -> run.set(true));

        //assert
        assertTrue(run.get());
        assertNotNull(result);
    }

    @Test
    public void testRunProtected_callable() throws Exception {
        //prepare
        Object expected = new Object();

        //act
        ExecutionResult<Object> result = ExecutionHelper.runProtected(() -> expected);

        //assert
        assertTrue(result.wasSuccess());
        assertEquals(expected, result.get());
    }

    @Test
    public void testRunProtected_runnable_withException() throws Exception {
        //prepare
        //act
        ExecutionResult result = ExecutionHelper.runProtected((Runnable) () -> {
            throw new IllegalArgumentException();
        });

        //assert
        assertNotNull(result);
        assertFalse(result.wasSuccess());
        assertTrue(result.getException().get() instanceof IllegalArgumentException);
    }

    @Test
    public void testRunProtected_callable_withException() throws Exception {
        //prepare
        //act
        ExecutionResult<Object> result = ExecutionHelper.runProtected(() -> {throw new IllegalArgumentException();});

        //assert
        assertNotNull(result);
        assertFalse(result.wasSuccess());
        assertTrue(result.getException().get() instanceof IllegalArgumentException);
    }
}

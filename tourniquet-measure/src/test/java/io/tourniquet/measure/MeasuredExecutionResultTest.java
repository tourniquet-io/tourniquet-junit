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

package io.tourniquet.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 *
 */
public class MeasuredExecutionResultTest {

    private Instant timestamp = Instant.now();
    private Duration duration = Duration.ofMillis(1500);

    @Test
    public void testGetReturnValue() throws Exception {
        //prepare
        Object returnValue = new Object();
        MeasuredExecutionResult<Object, Exception> result =
                new MeasuredExecutionResult<>(timestamp, duration, returnValue);

        //act
        Optional<Object> actual = result.getReturnValue();
        Optional<Exception> exception = result.getException();

        //assert
        assertEquals(returnValue, actual.get());
        assertFalse(exception.isPresent());
        assertTrue(result.wasSuccessful());
    }

    @Test
    public void testGetException() throws Exception {
        Exception t = new Exception();
        MeasuredExecutionResult<Object, Exception> result = new MeasuredExecutionResult<>(timestamp, duration, t);

        //act
        Optional<Object> actual = result.getReturnValue();
        Optional<Exception> exception = result.getException();

        //assert
        assertFalse(actual.isPresent());
        assertEquals(t, exception.get());
        assertFalse(result.wasSuccessful());
    }

    @Test
    public void testForReturnValue() throws Exception {
        //prepare
        Object returnValue = new Object();
        MeasuredExecutionResult<Object, Exception> result =
                new MeasuredExecutionResult<>(timestamp, duration, returnValue);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        MeasuredExecutionResult<Object, Exception> retVal = result.forReturnValue(holder::set);

        //assert
        assertSame(result, retVal);
        assertEquals(returnValue, holder.get());

    }

    @Test
    public void testForDuration() throws Exception {
        //prepare
        Object returnValue = new Object();
        MeasuredExecutionResult<Object, Exception> result =
                new MeasuredExecutionResult<>(timestamp, duration, returnValue);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        MeasuredExecutionResult<Object, Exception> retVal = result.forDuration(holder::set);

        //assert
        assertSame(result, retVal);
        assertEquals(duration, holder.get());

    }

    @Test
    public void testForException() throws Exception {
        //prepare
        Exception t = new Exception();
        MeasuredExecutionResult<Object, Exception> result = new MeasuredExecutionResult<>(timestamp, duration, t);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        MeasuredExecutionResult<Object, Exception> retVal = result.forException(holder::set);

        //assert
        assertSame(result, retVal);
        assertEquals(t, holder.get());

    }

    @Test(expected = Exception.class)
    public void testMapReturn_exception() throws Exception {
        //prepare
        Exception t = new Exception();
        MeasuredExecutionResult<Object, Exception> result = new MeasuredExecutionResult<>(timestamp, duration, t);

        //act
        result.mapReturn();

        //assert

    }

    @Test
    public void testMapReturn_null() throws Exception {
        //prepare
        MeasuredExecutionResult<Object, Exception> result =
                new MeasuredExecutionResult<>(timestamp, duration, null);
        //act
        Object retVal = result.mapReturn();

        //assert
        assertNull(retVal);

    }

    @Test
    public void testMapReturn_value() throws Exception {
        //prepare
        Object returnValue = new Object();
        MeasuredExecutionResult<Object, Exception> result =
                new MeasuredExecutionResult<>(timestamp, duration, returnValue);
        //act
        Object actual = result.mapReturn();

        //assert
        assertEquals(returnValue, actual);

    }

}

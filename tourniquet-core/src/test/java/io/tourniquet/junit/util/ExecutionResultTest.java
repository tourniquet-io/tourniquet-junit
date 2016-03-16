package io.tourniquet.junit.util;

import static org.junit.Assert.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 *
 */
public class ExecutionResultTest {

    @Test
    public void testForReturnValue() throws Exception {
        //prepare
        Object returnValue = new Object();
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(returnValue);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        ExecutionResult<Object> retVal = result.forReturnValue(holder::set);

        //assert
        assertSame(result, retVal);
        assertEquals(returnValue, holder.get());

    }

    @Test
    public void testCatchException() throws Exception {
        //prepare
        Exception t = new Exception();
        ExecutionResult<Object> result = ExecutionResult.ofException(t);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        ExecutionResult<Object> retVal = result.catchException(holder::set);

        //assert
        assertSame(result, retVal);
        assertEquals(t, holder.get());

    }

    @Test
    public void testCatchException_noException() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        ExecutionResult<Object> retVal = result.catchException(holder::set);

        //assert
        assertSame(result, retVal);
        assertNull(holder.get());

    }

    @Test
    public void testCatchException_typed_matching() throws Exception {
        //prepare
        Exception t = new IllegalArgumentException();
        ExecutionResult<Object> result = ExecutionResult.ofException(t);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        ExecutionResult<Object> retVal = result.catchException(IllegalArgumentException.class, holder::set);

        //assert
        assertSame(result, retVal);
        assertEquals(t, holder.get());
    }

    @Test
    public void testCatchException_typed_notMatching() throws Exception {
        Exception t = new IllegalStateException();
        ExecutionResult<Object> result = ExecutionResult.ofException(t);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        ExecutionResult<Object> retVal = result.catchException(IllegalArgumentException.class, holder::set);

        //assert
        assertSame(result, retVal);
        assertNull(holder.get());
    }

    @Test
    public void testCatchException_noException_nothing() throws Exception {
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(true);
        final AtomicReference<Object> holder = new AtomicReference<>();

        //act
        ExecutionResult<Object> retVal = result.catchException(IllegalArgumentException.class, holder::set);

        //assert
        assertSame(result, retVal);
        assertNull(holder.get());
    }


    @Test
    public void testGetException_withException() throws Exception {

        Exception t = new Exception();
        ExecutionResult<Object> result = ExecutionResult.ofException(t);

        //act
        Optional<Exception> exception = result.getException();

        //assert
        assertEquals(t, exception.get());
    }

    @Test
    public void testGetException_withoutException() throws Exception {

        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);

        //act
        Optional<Exception> exception = result.getException();

        //assert
        assertFalse(exception.isPresent());
    }

    @Test
    public void testWasSuccess_withException_false() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofException(new Exception());

        //act
        //assert
        assertFalse(result.wasSuccess());

    }

    @Test
    public void testWasSuccess_withoutException_true() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);

        //act
        //assert
        assertTrue(result.wasSuccess());
    }

    @Test
    public void testGetReturnValue_null() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);

        //act
        Optional<Object> retVal = result.getReturnValue();

        //assert
        assertNotNull(retVal);
        assertFalse(retVal.isPresent());
    }

    @Test
    public void testGetReturnValue_notNull() throws Exception {
        //prepare
        Object o = new Object();
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(o);

        //act
        Optional<Object> retVal = result.getReturnValue();

        //assert
        assertNotNull(retVal);
        assertEquals(o,retVal.get());
    }

    @Test
    public void testGet_null() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);

        //act
        Object retVal = result.get();

        //assert
        assertNull(retVal);
    }

    @Test
    public void testGet_object() throws Exception {
        //prepare
        Object o = new Object();
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(o);

        //act
        Object retVal = result.get();

        //assert
        assertEquals(o, retVal);
    }

    @Test(expected = Exception.class)
    public void testFlatten_exception() throws Exception {
        //prepare
        Exception t = new Exception();
        ExecutionResult<Object> result = ExecutionResult.ofException(t);

        //act
        result.flatten();

        //assert

    }

    @Test
    public void testFlatten_null() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);
        //act
        Object retVal = result.flatten();

        //assert
        assertNull(retVal);

    }

    @Test
    public void testFlatten_value() throws Exception {
        //prepare
        Object returnValue = new Object();
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(returnValue);
        //act
        Object actual = result.flatten();

        //assert
        assertEquals(returnValue, actual);

    }


    @Test
    public void testDoFinally() throws Exception {
        //prepare
        Object returnValue = new Object();
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(returnValue);
        final AtomicBoolean executed = new AtomicBoolean(false);

        //act
        result.doFinally(() -> executed.set(true));

        //assert
        assertTrue(executed.get());

    }

    @Test(expected = RuntimeException.class)
    public void testMapException() throws Exception {
        //prepare
        Exception t = new Exception();
        ExecutionResult<Object> result = ExecutionResult.ofException(t);

        //act
        result.mapException(Exception.class, RuntimeException::new);

        //assert
    }

    @Test
    public void testMapException_chainingReturnValue() throws Exception {
        //prepare
        ExecutionResult<Object> result = ExecutionResult.ofSuccess(null);

        //act
        ExecutionResult<Object> chainingValue = result.mapException(Exception.class, RuntimeException::new);
        assertEquals(result, chainingValue);
        //assert
    }

    @Test
    public void testOfVoid() throws Exception {
        //prepare

        //act
        ExecutionResult<Void> voidResult = ExecutionResult.ofVoid();

        //assert
        assertEquals(ExecutionResult.VOID, voidResult);

    }
}

package io.tourniquet.junit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test(expected = RuntimeException.class)
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

    @Test(expected = RuntimeException.class)
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

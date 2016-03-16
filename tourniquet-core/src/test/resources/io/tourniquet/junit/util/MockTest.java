package io.tourniquet.junit.util;

import static org.junit.Assert.fail;

import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class that to test the JUnit Runner
 */
public class MockTest {

    @Test
    public void testFail() throws Exception {

        fail();
    }

    @Test
    @Ignore
    public void testIgnore() throws Exception {

        throw new AssumptionViolatedException("ignored");
    }

    @Test
    public void testSuccess() throws Exception {

    }
}

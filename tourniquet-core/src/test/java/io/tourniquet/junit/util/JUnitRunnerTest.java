package io.tourniquet.junit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 *
 */
public class JUnitRunnerTest {

    @Test
    public void testRunClass() throws Exception {

        //prepare
        final ResourceClassLoader cl = new ResourceClassLoader();
        final Class<?> testClass = cl.loadClassFromResource("io.tourniquet.junit.util.MockTest","MockTest.clazz");

        //act
        Result result = JUnitRunner.runClass("io.tourniquet.junit.util.MockTest", () -> cl);

        //assert
        assertNotNull(result);
        assertEquals(1, result.getIgnoreCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(2, result.getRunCount());

        final List<Failure> failures = result.getFailures();
        assertNotNull(failures);
        assertEquals(1, failures.size());

    }
}

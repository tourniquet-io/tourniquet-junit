package io.tourniquet.junit.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 *
 */
public class ResultHelperTest {

    /**
     * The class under test
     */
    private ResultHelper subject = new ResultHelper();

    @Test
    public void testSerialize_deserialize() throws Exception {

        //prepare
        final ResourceClassLoader cl = new ResourceClassLoader();
        final Class<?> testClass = cl.loadClassFromResource("io.tourniquet.junit.util.MockTest","MockTest.clazz");
        final Computer computer = new Computer();
        final JUnitCore core = new JUnitCore();
        final Result input = core.run(computer, testClass);
        final Failure fi = input.getFailures().get(0);

        //act
        Result output = subject.deserialize(subject.serialize(input));

        //assert
        assertNotNull(output);
        assertEquals(input.getIgnoreCount(), output.getIgnoreCount());
        assertEquals(input.getFailureCount(), output.getFailureCount());
        assertEquals(input.getRunCount(), output.getRunCount());
        assertEquals(input.getRunTime(), output.getRunTime());
        assertEquals(1, output.getIgnoreCount());
        assertEquals(1, output.getFailureCount());
        assertEquals(2, output.getRunCount());

        final List<Failure> failures = output.getFailures();
        assertNotNull(failures);
        assertEquals(input.getFailures().size(), failures.size());

        Failure fo = failures.get(0);
        assertEquals(fi.getDescription().getDisplayName(), fo.getDescription().getDisplayName());
        assertEquals(fi.getDescription().getClassName(), fo.getDescription().getClassName());
        assertEquals(fi.getException().getMessage(), fo.getException().getMessage());
        assertArrayEquals(fi.getException().getStackTrace(), fo.getException().getStackTrace());

    }

}

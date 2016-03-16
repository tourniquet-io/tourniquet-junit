package io.tourniquet.junit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 * Helper class for transfering Results object from one Classloader hierarchy to another.
 */
public class ResultHelper {

    /**
     * Serializes the result into a binary representation. To ensure it can be deserialized without the test classes,
     * all references to test files are removed by transfering to Java basic classes.
     *
     * @param result
     *         the result to be serialized
     *
     * @return a serialized representation of the result
     *
     * @throws Exception
     *         if the result can not be deserialized
     */
    public byte[] serialize(Result result) throws Exception {

        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(os);
        oos.writeObject(copy(result));
        return os.toByteArray();
    }

    /**
     * Deserializes the specified byte-array into a {@link org.junit.runner.Result}.
     *
     * @param result
     *         a binary representation of the result
     *
     * @return the deserialized result.
     *
     * @throws IOException
     *         if the byte-array could not be deserialized
     * @throws ClassNotFoundException
     *         if the required classes to deserialize the byte-array could not be found
     */
    public Result deserialize(byte[] result) throws IOException, ClassNotFoundException {

        return (Result) new ObjectInputStream(new ByteArrayInputStream(result)).readObject();
    }

    private Result copy(Result old) throws Exception {

        final Result result = new Result();
        copyFields(old, result);
        copyFailures(old, result);
        return result;
    }

    private void copyFields(final Result old, final Result result) throws NoSuchFieldException, IllegalAccessException {

        final AtomicInteger count = getAccessibleResultField(result, "count");
        count.set(old.getRunCount());

        final AtomicInteger ignoreCount = getAccessibleResultField(result, "ignoreCount");
        ignoreCount.set(old.getIgnoreCount());

        final AtomicLong runTime = getAccessibleResultField(result, "runTime");
        runTime.set(old.getRunTime());

        final AtomicLong startTime = getAccessibleResultField(result, "startTime");
        startTime.set(((AtomicLong) getAccessibleResultField(old, "startTime")).get());
    }

    private void copyFailures(final Result old, final Result result)
            throws NoSuchFieldException, IllegalAccessException {

        final CopyOnWriteArrayList<Failure> failures = getAccessibleResultField(result, "failures");
        old.getFailures().stream().map(of -> {
            final Throwable nt = copyThrowable(of.getException());
            final Description nd = copyDescription(of.getDescription());
            return new Failure(nd, nt);

        }).collect(Collectors.toCollection(() -> failures));
    }

    private Throwable copyThrowable(Throwable ot) {

        final Throwable nt = new Throwable(ot.getMessage());
        nt.setStackTrace(ot.getStackTrace());
        return nt;
    }

    private Description copyDescription(Description desc) {

        return desc.getChildren()
                   .stream()
                   .map(this::copyDescription)
                   .collect(() -> Description.createTestDescription(desc.getClassName(), desc.getMethodName()),
                            Description::addChild,
                            (d1, d2) -> {
                            });

    }

    @SuppressWarnings("unchecked")
    private <T> T getAccessibleResultField(Result result, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {

        final Field field = Stream.of(Result.class.getDeclaredFields())
                                  .filter(f -> f.getName().contains(fieldName))
                                  .findFirst()
                                  .orElseThrow(() -> new NoSuchFieldException("No field found matching " + fieldName));
        field.setAccessible(true);
        return (T) field.get(result);
    }
}

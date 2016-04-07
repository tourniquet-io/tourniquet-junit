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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.slf4j.Logger;

/**
 * Collector for recording response times. The collector has two collections. One per thread and one single global
 * collection. The thread local collection should meet most cases. But in some cases it might be required to collect
 * response times centrally for multiple threads that collect their response times using the {@link
 * ResponseTimeCollector}.
 * <p/>
 * As default setting, the response times are stored in a global collection. This class allows to record response times
 * of transaction across multiple threads. It must be ensured, that the response are cleared after the measured
 * sequence, otherwise the response remain in memory permanently.
 */
public final class ResponseTimes {

    private static final Logger LOG = getLogger(ResponseTimes.class);

    /**
     * Global collector
     */
    private static final ResponseTimes GLOBAL_RESPONSE_TIMES = new ResponseTimes();
    /**
     * Flag to indicate, that response times should be collected globally, which is disabled by default
     */
    private static final AtomicBoolean GLOBAL_COLLECTION_ENABLED = new AtomicBoolean(false);

    private static final ThreadLocal<ResponseTimes> LOCAL = ThreadLocal.withInitial(ResponseTimes::new);
    /**
     * ResponseTime collection.
     */
    private final Map<UUID, ResponseTime> times = new ConcurrentHashMap<>();

    /**
     * Default consumer putting a response time into the global table
     */
    private final Consumer<ResponseTime> defaultConsumer = rt -> times.put(rt.getUuid(), rt);

    private final AtomicReference<Consumer<ResponseTime>> startTxConsumer = new AtomicReference<>();

    private final AtomicReference<Consumer<ResponseTime>> stopTxConsumer = new AtomicReference<>();

    private final AtomicReference<ScheduledFuture> cleanupStrategy = new AtomicReference<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final AtomicBoolean forwardToGlobal = new AtomicBoolean(false);

    private ResponseTimes() {

    }

    /**
     * The response time collector for the current thread. Every thread has it's own collector, but forwarding to the
     * global collection can be enabled for all or single threads.
     *
     * @return the current response times collector
     */
    public static ResponseTimes current() {

        return LOCAL.get();
    }

    /**
     * A global collector shared by all threads.
     *
     * @return the global response time collector
     */
    public static ResponseTimes global() {

        return GLOBAL_RESPONSE_TIMES;
    }

    /**
     * Clears the response time collection
     */
    public void clear() {

        times.clear();
    }

    /**
     * Enables the global response time collection. All per-thread recorded response times are also collected globally.
     * Be aware that in scenarios where a log of response times should be collected, the global collection may lead to
     * an out of memory in long term and should be cleared regularly.
     *
     * @param enabled
     *         <code>true</code> if the response times should be collected globally, <code>false</code> if not.
     */
    public static void enableGlobalCollection(boolean enabled) {

        GLOBAL_COLLECTION_ENABLED.set(enabled);
    }

    /**
     * Enables response time forwarding to the global collection for the current thread.
     *
     * @param enabled
     *         <code>true</code> if the response times should be forwared, <code>false</code> if not.
     */
    public void enableForwardToGlobal(boolean enabled) {

        forwardToGlobal.set(enabled);
    }

    /**
     * Overrides the default consumer that handles response times on the beginning of a transaction
     *
     * @param responseTimeConsumer
     *         the consumer to process response times
     */
    public void onMeasureStart(Consumer<ResponseTime> responseTimeConsumer) {

        startTxConsumer.set(responseTimeConsumer);
    }

    /**
     * Overrides the default consumer that handles response times on the end of a transaction
     *
     * @param responseTimeConsumer
     *         the consumer to process response times
     */
    public void onMeasureEnd(Consumer<ResponseTime> responseTimeConsumer) {

        stopTxConsumer.set(responseTimeConsumer);
    }

    /**
     * Sets a cleanup strategy how to cleanup the collected times. Default strategy will keep all response times.
     *
     * @param cleanupStrategy
     *         the cleanup strategy to be operated on the response time map.
     * @param interval
     *         the interval between the invocations of the cleanup strategy
     */
    public void setCleanupStrategy(Consumer<Map<UUID, ResponseTime>> cleanupStrategy, Duration interval) {

        Optional.ofNullable(this.cleanupStrategy.get()).ifPresent(f -> f.cancel(true));
        Optional.ofNullable(cleanupStrategy).ifPresent(cs -> {
            Objects.requireNonNull(interval);
            final long period = interval.toMillis();
            this.cleanupStrategy.set(this.scheduler.scheduleAtFixedRate(() -> cs.accept(this.times),
                                                                        period,
                                                                        period,
                                                                        TimeUnit.MILLISECONDS));
        });

    }

    /**
     * Starts a transaction on the specified point in time
     *
     * @param transaction
     *         the transaction to start
     *
     * @return the ResponseTime handle to track the time measure
     */
    public ResponseTime startTx(String transaction) {

        return startTx(transaction, Instant.now());
    }

    /**
     * Starts a transaction on the specified point in time
     *
     * @param transaction
     *         the transaction to start
     * @param start
     *         the point in time when the transaction started
     *
     * @return the ResponseTime handle for this response time measure
     */
    public ResponseTime startTx(String transaction, Instant start) {

        final ResponseTime responseTime = new ResponseTime(transaction, start);
        startTx(responseTime);
        return responseTime;
    }

    void startTx(final ResponseTime responseTime) {

        defaultConsumer.accept(responseTime);
        Optional.ofNullable(startTxConsumer.get()).ifPresent(c -> c.accept(responseTime));
        if (isGlobalCollectionEnabled()) {
            global().startTx(responseTime);
        }
    }

    private boolean isGlobalCollectionEnabled() {

        return this != GLOBAL_RESPONSE_TIMES && (forwardToGlobal.get() || GLOBAL_COLLECTION_ENABLED.get());
    }

    /**
     * Records the response time as finished.
     *
     * @param finish
     *         the response time of the finished transaction. If the response time denotes no finished transaction, it
     *         is finished now.
     *
     * @return the finished ResponseTime
     */
    public ResponseTime stopTx(ResponseTime finish) {

        return collect(finish.isFinished() ? finish : finish.finish());
    }

    /**
     * Collects a completed ResponseTime recording.
     *
     * @param responseTime
     *         the responseTime to collect into the global collection.
     */
    public ResponseTime collect(final ResponseTime responseTime) {

        if (!responseTime.isFinished()) {
            throw new AssertionError("Collecting of unfinished responseTimes is not allowed");
        }
        defaultConsumer.accept(responseTime);
        Optional.ofNullable(stopTxConsumer.get()).ifPresent(c -> c.accept(responseTime));
        if (isGlobalCollectionEnabled()) {
            global().collect(responseTime);
        }
        return responseTime;
    }

    /**
     * Collects a completed time measure for a specific transaction.
     *
     * @param transaction
     *         the transaction to be associated with the time measure
     * @param result
     *         a time measure result to associate with the transaction
     *
     * @return the response time recorded
     */
    public ResponseTime collect(String transaction, SimpleTimeMeasure result) {

        return collect(new ResponseTime(transaction, result.getStart(), result.getDuration()));
    }

    /**
     * Returns all recorded response times.
     *
     * @return a map of the response times. The map contains the transaction names as key, and a list of measured
     * responseTimes for that transaction as value.
     */
    public Map<String, List<ResponseTime>> getResponseTimes() {

        final Map<String, List<ResponseTime>> result = new HashMap<>();
        times.values().stream().forEach(trt -> {
            if (!result.containsKey(trt.getTransaction())) {
                result.put(trt.getTransaction(), new ArrayList<>());
            }
            result.get(trt.getTransaction()).add(trt);
        });
        return result;
    }

    /**
     * Collects response times collected by the current thread in another classloader hierarchy.
     * @param cl
     *  the classloader from which the response times should be collected
     * @return
     *  the collected results
     */
    public static Map<String, List<ResponseTime>> getCurrentResponseTimes(ClassLoader cl) {

        return getResponseTimeObject(cl, "current");
    }

    /**
     * Collects response times collected by the global collector in another classloader hierarchy.
     * @param cl
     *  the classloader from which the response times should be collected
     * @return
     *  the collected results
     */
    public static Map<String, List<ResponseTime>> getGlobalResponseTimes(ClassLoader cl) {

        return getResponseTimeObject(cl, "global");
    }

    private static Map<String, List<ResponseTime>> getResponseTimeObject(final ClassLoader cl,
                                                                         final String methodName) {

        try {
            final Class<?> responseTimesClass = cl.loadClass(ResponseTimes.class.getName());
            final Object oCurrent = responseTimesClass.getMethod(methodName).invoke(null);
            final Object oResponseTimes = responseTimesClass.getMethod("getResponseTimes").invoke(oCurrent);
            return transfer(oResponseTimes);
        } catch (Exception e) {
            LOG.error("Unable to retrieve response times", e);
        }
        return Collections.emptyMap();
    }

    /**
     * Tansfers a given object by serializing and deserializing from one classloader hierarchy to another, given both
     * classloader hierarchies contain the same classes.
     *
     * @param object
     *         the Object to transfer
     *
     * @return the transferred object
     */
    private static <TARGET_TYPE> TARGET_TYPE transfer(Object object) throws IOException, ClassNotFoundException {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        return (TARGET_TYPE) ois.readObject();
    }
}

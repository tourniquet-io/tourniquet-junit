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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/**
 * Collector for recording response times. The collector has two collections. One per thread and one single global
 * collection. The thread local collection should meet most cases. But in some cases it might be required
 * to collect response times centrally for multiple thread that collect their response times using the
 * {@link ResponseTimeCollector}.
 *
 * As default setting, the response times are stored in a global collection.
 * This class allows to record response times of transaction across multiple threads. It must be ensured, that the
 * response are cleared after the measured sequence, otherwise the response remain in memory permanently.
 */
public final class ResponseTimes {

    /**
     * Global collector
     */
    private static final ResponseTimes GLOBAL = new ResponseTimes();
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

    private ResponseTimes(){}

    public static ResponseTimes current(){
        return LOCAL.get();
    }

    public static ResponseTimes global() {
        return GLOBAL;
    }

    /**
     * Clears the global response time collection
     */
    public void clear() {

        times.clear();
    }

    /**
     * Enables the global response time collection. All per-thread recorded response times are also collected
     * globally.
     * Be aware that in scenarios where a log of response times should be collected, the global collection
     * may lead to an out of memory in long term and should be cleared regularly.
     * @param enabled
     *  <code>true</code> if the response times should be collected globally, <code>false</code> if not.
     */
    public static void enableGlobalCollection(boolean enabled){
        GLOBAL_COLLECTION_ENABLED.set(true);
    }

    /**
     * Overrides the default consumer that handles response times on the beginning of a transaction
     * @param responseTimeConsumer
     *  the consumer to process response times
     */
    public void onMeasureStart(Consumer<ResponseTime> responseTimeConsumer){
        startTxConsumer.set(responseTimeConsumer);
    }

    /**
     * Overrides the default consumer that handles response times on the end of a transaction
     * @param responseTimeConsumer
     *  the consumer to process response times
     */
    public void onMeasureEnd(Consumer<ResponseTime> responseTimeConsumer){
        stopTxConsumer.set(responseTimeConsumer);
    }

    /**
     * Sets a cleanup strategy how to cleanup the collected times. Default strategy will keep all response times.
     * @param cleanupStrategy
     *  the cleanup strategy to be operated on the response time map.
     * @param interval
     *  the interval between the invocations of the cleanup strategy
     */
    public void setCleanupStrategy(Consumer<Map<UUID,ResponseTime>> cleanupStrategy, Duration interval){

        Optional.ofNullable(this.cleanupStrategy.get()).ifPresent(f -> f.cancel(true));
        final long period = interval.toMillis();
        this.cleanupStrategy.set(this.scheduler.scheduleAtFixedRate(() -> cleanupStrategy.accept(this.times),
                                                                    period, period, TimeUnit.MILLISECONDS));
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
        if(GLOBAL_COLLECTION_ENABLED.get()){
            global().startTx(responseTime);
        }
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
    public  ResponseTime stopTx(ResponseTime finish) {

        return collect(finish.isFinished() ? finish : finish.finish());
    }

    /**
     * Collects a completed ResponeTime recording.
     *
     * @param responseTime
     *         the responseTime to collect into the global collection.
     */
    public  ResponseTime collect(final ResponseTime responseTime) {

        if (!responseTime.isFinished()) {
            throw new AssertionError("Collecting of unfinished responseTimes is not allowed");
        }
        defaultConsumer.accept(responseTime);
        Optional.ofNullable(stopTxConsumer.get()).ifPresent(c -> c.accept(responseTime));
        if(GLOBAL_COLLECTION_ENABLED.get()){
            global().collect(responseTime);
        }
        return responseTime;
    }

    /**
     * Collects a completed time measure for a specific transaction.
     * @param transaction
     *  the transaction to be associated with the time measure
     * @param result
     *  a time measure result to associate with the transaction
     * @return
     *  the response time recorded
     */
    public  ResponseTime collect(String transaction, SimpleTimeMeasure result) {
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
}

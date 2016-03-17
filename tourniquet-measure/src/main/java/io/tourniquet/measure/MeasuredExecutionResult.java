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
import java.util.function.Consumer;

import io.tourniquet.junit.util.ExecutionResult;

/**
 * The result of a time measured execution. As this measurement holds a reference to the return value of a measured
 * execution (or the throwable that occured during the execution) you should not keep it in memory for recording
 * execution times as this will fill up memory.
 */
public class MeasuredExecutionResult<RESULTTYPE> extends ExecutionResult<RESULTTYPE> implements TimeMeasure {

    private final SimpleTimeMeasure measure;

    public MeasuredExecutionResult(final Instant start, final Duration duration, RESULTTYPE result) {

        super(result, null);
        this.measure = new SimpleTimeMeasure(start, duration);
    }

    public MeasuredExecutionResult(final Instant start, final Duration duration, Exception throwable) {

        super(null, throwable);
        this.measure = new SimpleTimeMeasure(start, duration);
    }

    /**
     * Method to fluently process the duration of the measured operation.
     *
     * @param consumer
     *         the consumer to process the duration
     *
     * @return this measure
     */
    public MeasuredExecutionResult<RESULTTYPE> forDuration(Consumer<Duration> consumer) {

        consumer.accept(getDuration());
        return this;
    }

    @Override
    public Instant getStart() {

        return this.measure.getStart();
    }

    @Override
    public Duration getDuration() {

        return this.measure.getDuration();
    }

    @Override
    public boolean isFinished() {

        return this.measure.isFinished();
    }
}

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

/**
 * Defines a result of a time measurement. Every time measurement started at a specific point in time and - if
 * the measurement was finished - has a duration.
 */
public interface TimeMeasure {

    /**
     * Constant for defining a negative time measure. In terms of measuring the time, this is an indicator
     * of not finishing the measurement.
     */
    Duration NEGATIVE = Duration.ZERO.minus(Duration.ofMillis(1));

    /**
     * Denotes the starting point of the time measurement.
     * @return
     *  the starting time of the measurement
     */
    Instant getStart();

    /**
     * The measured time
     * @return
     *  the duration of the time measurement
     */
    Duration getDuration();

    /**
     * Indicates whether the time measurement was finished.
     * @return
     *  true if the time measurement has a non-negative duration.
     */
    boolean isFinished();

}

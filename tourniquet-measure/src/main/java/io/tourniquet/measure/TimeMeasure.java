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

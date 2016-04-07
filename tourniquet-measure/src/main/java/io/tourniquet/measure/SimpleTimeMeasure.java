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

import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

/**
 * Represents a single time measure. The time measure can either denote a specific point in time - being an unfinished
 * measure - or a specific time (the start of the measurement) and a duration the measured operation took.
 */
public class SimpleTimeMeasure implements TimeMeasure, Serializable {

    private static final long serialVersionUID = -686596868634888819L;
    private final Instant start;
    private final Duration duration;

    public SimpleTimeMeasure(final Instant start, final Duration duration) {

        this.start = start;
        this.duration = duration;
    }

    public SimpleTimeMeasure(final Instant start) {
        this(start, NEGATIVE);
    }

    @Override
    public Instant getStart() {

        return start;
    }

    @Override
    public Duration getDuration() {

        return duration;
    }

    @Override
    public boolean isFinished(){
        return duration != NEGATIVE;
    }

}

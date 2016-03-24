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

package io.tourniquet.pageobjects;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fluent builder for composite waits. The builder can be used to compose a chain of waits and specify a total
 * timeout for all waits. Note that the chain is flushed on every invocation of {@link #within(java.time.Duration)}
 * that way a multi-phased wait can be declared.
 * <br>
 * For example:
 * <pre><code>
 *     WaitChain.wait(activeWait1)
 *         .andThen(activeWait2)
 *         .within(timeoutForWait1And2)
 *         .andThen(activeWait3)
 *         .within(timeoutForWait3);
 * </code>
 * </pre>
 */
public class WaitChain {

    private final List<ActiveWait> waits = new ArrayList<>();

    private WaitChain(ActiveWait initialWait) {

        this.waits.add(initialWait);
    }

    /**
     * Create new composite wait starting with the initial wait activity.
     * @param initialWait
     *  the initial wait activity to start the wait chain
     * @return
     *  a new WaitChain instance
     */
    public static WaitChain wait(ActiveWait initialWait) {
        Objects.requireNonNull(initialWait);
        return new WaitChain(initialWait);
    }

    /**
     * Appends a wait activity to the wait chain.
     * @param wait
     *  the next wait activity
     * @return
     *  this WaitChain instance
     */
    public WaitChain andThen(ActiveWait wait) {
        Objects.requireNonNull(wait);
        this.waits.add(wait);
        return this;
    }

    /**
     * Triggers the wait activities of the chain wait the specified timeout is reached.
     * @param timeout
     *  the total timeout for all wait activities in the chain
     * @return
     *  this wait. In case no timeout occurred, additional wait activities can be added.
     */
    public WaitChain within(Duration timeout) {
        Objects.requireNonNull(timeout);
        within(timeout, 0);
        waits.clear();
        return this;
    }

    /**
     * Walks through the wait chain, skipping the first elements
     * @param timeout
     *  the time left for the remainder of the chain
     * @param skipWaits
     *  number of elements in the chain to skip
     */
    private void within(Duration timeout, int skipWaits) {
        if (timeout.isNegative()) {
            return;
        }
        waits.stream()
             .skip(skipWaits)
             .findFirst()
             .map(wait -> timeLeftAfter(wait, timeout))
             .ifPresent(timeleft -> within(timeleft, skipWaits + 1));
    }

    /**
     * Performs the wait and returns the time that is left after the wait
     * @param wait
     *  the active wait to be performed
     * @param timeLeftBefore
     *  the time left befor the active wait
     * @return
     *  the time left after the active wait
     */
    private Duration timeLeftAfter(ActiveWait wait, Duration timeLeftBefore) {

        final Instant before = Instant.now();
        wait.wait(timeLeftBefore);
        return timeLeftBefore.minus(Duration.between(before, Instant.now()));
    }
}

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
import java.util.function.Function;

/**
 * Provider for timeouts. Using this this provider, timeouts can be configured in order to adapt
 * wait cycles to either a changing test environment or to chaning non-functional requirements.
 */
@FunctionalInterface
public interface TimoutProvider extends Function<String, Duration> {

    /**
     * Provides the duration for a timeout for the specified wait point.
     * @param timeoutKey
     *  the unique name for the timeout
     * @return
     *  the duration how long should be waited in order to trigger a timeout.
     */
    Duration getTimeoutFor(String timeoutKey);

    @Override
    default Duration apply(String s) {
        return getTimeoutFor(s);
    }
}

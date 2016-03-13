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

package io.tourniquet.junit.matchers;

import java.util.concurrent.TimeUnit;

import org.hamcrest.Matcher;

/**
 * Interface to be implemented by Matchers that require a timeout. By implementing this method the matcher
 * becomes configurable to produce a result within a certain timeframe.
 *
 * Created by <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a> on 3/13/2015
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public interface TimeoutSupport {

    /**
     * Specifies the timeout for the matcher. If the the matcher does not produce a result within this specified
     * timeframe it should fail. If milliseconds are required, the implementation of this method could simply be
     * <pre><code>
     *     long timeoutInMs = timeUnit.toMillis(duration);
     * </code></pre>
     * @param duration
     *  the duration to wait until timing out
     * @param timeUnit
     *  the time unit for the duration
     * @param <T>
     *     the matcher itself
     * @return
     *  the matcher itself
     */
    <T extends Matcher<?>> T within(long duration, TimeUnit timeUnit);
}

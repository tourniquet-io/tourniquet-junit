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

import static io.tourniquet.pageobjects.TimeoutProvider.DEFAULT_TIMEOUT;

import java.time.Duration;
import java.util.Optional;

/**
 * Utility class to get timeout values for various criteria. <br> There are various options for define or obtain a
 * timeout value. The order of search for the concrete value is as follows, returning the first value that is defined.
 * <ol> <li>locator <ol> <li>timeout (by key)</li> <li>timeout (fixed value)</li> </ol> </li> <li> timeout (by key)
 * </li> <li> timeout (fixed value) </li> </ol>
 */
public final class Timeouts {

    private Timeouts() {

    }

    public static Optional<Duration> getOptionalTimeout(Locator locator) {

        if ("".equals(locator.timeoutKey())) {
            return Optional.of(Duration.ofSeconds(locator.timeout()));
        }
        return Optional.of(getTimeout(locator.timeoutKey()));
    }

    /**
     * Gets the effective timeout for a given locator
     *
     * @param loc
     *         the locator to get the timeout for. If the locator has no timeoutKey specified, the actual timeout value
     *         of the locator is used. If the timeout key is set, it is resolved using the timeout provider of the
     *         current {@link SeleniumContext}.
     *
     * @return
     */
    public static Duration getTimeout(final Locator loc) {

        return getOptionalTimeout(loc).orElse(DEFAULT_TIMEOUT);
    }

    /**
     * Gets the timeout for the specified key. The timeout is resolved using the provider of the current
     * {@link SeleniumContext}. If no such timeout is defined or the context is not initialized, the default timeout
     * is returned.
     *
     * @param timeoutKey
     *  the key of the timeout to retrieve
     * @return
     *  the duration when to time out
     */
    public static Duration getTimeout(final String timeoutKey) {

        return SeleniumContext.currentContext()
                              .map(SeleniumContext::getTimeoutProvider)
                              .flatMap(tp -> tp.getTimeoutFor(timeoutKey))
                              .orElse(DEFAULT_TIMEOUT);
    }

    /**
     * Gets the timeout for a locator or use a default if the locator is empty. When a locator is provided, its timeout
     * respectively its configured timeout is used.
     *
     * @param locator
     *         the locator
     * @param fallbackTimeoutKey
     *         the key for the fallback timeout in case there is no locator present.
     *
     * @return the duration for timing out
     */
    public static Duration getTimeout(Optional<Locator> locator, String fallbackTimeoutKey) {

        return locator.map(Timeouts::getTimeout).orElse(getTimeout(fallbackTimeoutKey));
    }

    public static Duration getTimeout(Optional<Locator> locator, String fallbacKey, long fallbackTimeoutMillis) {

        return null;
    }

}

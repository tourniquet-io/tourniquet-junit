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

import static io.tourniquet.selenium.SeleniumContext.currentContext;
import static io.tourniquet.selenium.TimeoutProvider.DEFAULT_TIMEOUT;

import java.time.Duration;
import java.util.Optional;

import io.tourniquet.selenium.SeleniumContext;

/**
 * Utility class to get timeout values for various criteria. <br> There are various options for define or obtain a
 * timeout value. The order of search for the concrete value is as follows, returning the first value that is defined.
 * <ol> <li>locator <ol> <li>timeout (by key)</li> <li>timeout (fixed value)</li> </ol> </li> <li> timeout (by key)
 * </li> <li> timeout (fixed value) </li> </ol>
 */
public final class Timeouts {

    private Timeouts() {

    }

    /**
     * Gets the timeout for the element specified by locator. If the locator defines a timeout by key but no timeout for
     * that key is defined (see {@link #getOptionalTimeout(String)}), the result will be empty.
     *
     * @param locator
     *         the locator of the element whose timeout should be retrieved
     *
     * @return the timeout for the provided key if the context is set and the a timeout for the key is provided.
     */
    public static Optional<Duration> getOptionalTimeout(Locator locator) {

        if ("".equals(locator.timeoutKey())) {
            return Optional.of(Duration.ofSeconds(locator.timeout()));
        }
        return getOptionalTimeout(locator.timeoutKey());
    }

    /**
     * Gets the timeout for the specified key. The timeout is retrieved from the timeout provider of the current {@link
     * io.tourniquet.selenium.SeleniumContext}. The result will be empty if the context is not initialized or no timeout
     * is defined for the given key.
     *
     * @param timeoutKey
     *         the key of the the timeout to retrieve
     *
     * @return the timeout for the provided key if the context is set and the a timeout for the key is provided.
     */
    public static Optional<Duration> getOptionalTimeout(String timeoutKey) {

        return currentContext().getTimeoutProvider().getTimeoutFor(timeoutKey);
    }

    /**
     * Gets the effective timeout for a given locator
     *
     * @param loc
     *         the locator to get the timeout for. If the locator has no timeoutKey specified, the actual timeout value
     *         of the locator is used. If the timeout key is set, it is resolved using the timeout provider of the
     *         current {@link SeleniumContext}.
     *
     * @return the duration when to timeout, specified by the locator's timeout key or the locator timeout if no
     * configured timeout was found.
     */
    public static Duration getTimeout(final Locator loc) {

        return getOptionalTimeout(loc).orElse(Duration.ofSeconds(loc.timeout()));
    }

    /**
     * Gets the timeout for the specified key. The timeout is resolved using the provider of the current {@link
     * SeleniumContext}. If no such timeout is defined or the context is not initialized, the default timeout is
     * returned.
     *
     * @param timeoutKey
     *         the key of the timeout to retrieve
     *
     * @return the duration when to time out or the default timeout if no configured timeout was found for the key.
     */
    public static Duration getTimeout(final String timeoutKey) {

        return currentContext().getTimeoutProvider().getTimeoutFor(timeoutKey).orElse(DEFAULT_TIMEOUT);
    }

    /**
     * Gets a timeout duration from the specified locator. If the locator is empty the fallback timeout is used.
     *
     * @param locator
     *         the locator providing the timeout. may be empty.
     * @param fallbackTimeoutMillis
     *         the timeout in milliseconds that is returned if the locator is empty
     *
     * @return the timeout duration
     */
    public static Duration getTimeout(Optional<Locator> locator, long fallbackTimeoutMillis) {

        return locator.map(Timeouts::getTimeout).orElse(Duration.ofMillis(fallbackTimeoutMillis));
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

        return locator.flatMap(Timeouts::getOptionalTimeout).orElse(getTimeout(fallbackTimeoutKey));
    }

    /**
     * Gets the timeout for a locator or use a default if the locator is empty. When a locator is provided, its timeout
     * respectively its configured timeout is used. Note that in case the locator defines a timeout key but no timeout
     * is configured for that key, the fallbacks specified for this method are used instead of the locator's timeout
     * value <br> If the locator is empty, the configured timeout for the specified fallback key is used. If no timeout
     * is configured for that key, the fallback timeout (milliseconds) is used.
     *
     * @param locator
     *         the locator from which to obtain the timeout. If the locator defines no timeout key, the timeout value is
     *         used. If a key is defined, the configured timeout for that key is used. If the no timeout is configured
     *         for that key, the fallbacks apply.
     * @param fallbacKey
     *         the key of the configured timeout that should be used, if the locator is empty
     * @param fallbackTimeoutMillis
     *         the timeout in milliseconds that is used in case the locator is empty and no timeout is configured for
     *         the specified fallback key.
     *
     * @return the timeout for the given parameters
     */
    public static Duration getTimeout(Optional<Locator> locator, String fallbacKey, long fallbackTimeoutMillis) {

        return locator.flatMap(Timeouts::getOptionalTimeout)
                      .orElse(getOptionalTimeout(fallbacKey).orElse(Duration.ofMillis(fallbackTimeoutMillis)));
    }

}

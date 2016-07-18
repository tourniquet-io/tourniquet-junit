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

package io.tourniquet.selenium;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Duration;
import java.util.Optional;

import io.tourniquet.selenium.TimeoutProvider;
import org.junit.Test;

/**
 *
 */
public class TimeoutProviderTest {

    @Test
    public void testApply() throws Exception {

        //prepare
        TimeoutProvider provider = timeoutKey -> Optional.of(Duration.ofSeconds(1234));

        //act
        Optional<Duration> result = provider.apply("someKey");

        //assert
        assertEquals(Duration.ofSeconds(1234), result.get());
    }

    @Test
    public void testDefaultProvider() throws Exception {
        //prepare

        //act
        Optional<Duration> result = TimeoutProvider.DEFAULT_PROVIDER.getTimeoutFor("anyKey");

        //assert
        assertNotNull(result);
        assertEquals(TimeoutProvider.DEFAULT_TIMEOUT, result.get());

    }
}

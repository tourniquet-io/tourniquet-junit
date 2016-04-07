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

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 *
 */
public class ActiveWaitTest {

    @Test
    public void testAccept() throws Exception {

        //prepare
        AtomicReference<Duration> duration = new AtomicReference<>();
        ActiveWait wait = new ActiveWait() {

            @Override
            public void wait(final Duration timeout) {
                duration.set(timeout);
            }
        };


        //act
        wait.accept(Duration.ZERO);

        //assert
        assertEquals(Duration.ZERO, duration.get());

    }
}

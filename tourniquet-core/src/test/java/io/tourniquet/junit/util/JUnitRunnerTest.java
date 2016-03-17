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

package io.tourniquet.junit.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

/**
 *
 */
public class JUnitRunnerTest {

    @Test
    public void testRunClass() throws Exception {

        //prepare
        final ResourceClassLoader cl = new ResourceClassLoader();
        final Class<?> testClass = cl.loadClassFromResource("io.tourniquet.junit.util.MockTest","MockTest.clazz");

        //act
        Result result = JUnitRunner.runClass("io.tourniquet.junit.util.MockTest", () -> cl);

        //assert
        assertNotNull(result);
        assertEquals(1, result.getIgnoreCount());
        assertEquals(1, result.getFailureCount());
        assertEquals(2, result.getRunCount());

        final List<Failure> failures = result.getFailures();
        assertNotNull(failures);
        assertEquals(1, failures.size());

    }
}

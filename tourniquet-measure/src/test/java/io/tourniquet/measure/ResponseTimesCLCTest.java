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

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map;

import io.tourniquet.junit.util.JUnitRunner;
import io.tourniquet.junit.util.ResourceResolver;
import io.tourniquet.junit.util.TestClassLoader;
import org.junit.Test;
import org.junit.runner.Result;

/**
 * Classloader Crossing Test
 */
public class ResponseTimesCLCTest {

    private ResourceResolver resolver = new ResourceResolver(true);

    @Test
    public void testGetCurrentResponseTimeRetrieval() throws Exception {
        //prepare
        try(TestClassLoader cl = new TestClassLoader(singletonList(resolver.resolve("/test.zip")))) {
            Result result = JUnitRunner.runClass("io.tourniquet.measure.MockTest", () -> cl);
            assertEquals(2, result.getRunCount());
            //act
            Map<String, List<ResponseTime>> rts = ResponseTimes.getCurrentResponseTimes(cl);
            //assert
            assertNotNull(rts);
            assertEquals(1, rts.size());
            assertEquals(3, rts.get("current").size());
        }
    }

    @Test
    public void testGlobalResponseTimesRetrieval() throws Exception {
        //prepare
        try(TestClassLoader cl = new TestClassLoader(singletonList(resolver.resolve("/test.zip")))) {
            Result result = JUnitRunner.runClass("io.tourniquet.measure.MockTest", () -> cl);
            assertEquals(2, result.getRunCount());
            //act
            Map<String, List<ResponseTime>> rts = ResponseTimes.getGlobalResponseTimes(cl);
            //assert
            assertNotNull(rts);
            assertEquals(2, rts.size());
            assertEquals(9, rts.get("global").size());
            assertEquals(3, rts.get("local").size());
        }

    }
}

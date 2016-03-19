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

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

/**
 * Test class to generate response times
 */
public class MockTest {

    @Test
    public void testCurrentResponseTimes() throws Exception {
        ResponseTimes current = ResponseTimes.current();
        ResponseTimes.enableGlobalCollection(false);
        ResponseTimes.current().clear();
        current.stopTx(current.startTx("current"));
        current.stopTx(current.startTx("current"));
        current.stopTx(current.startTx("current"));
    }

    @Test
    public void testGlobalResponseTimes() throws Exception {

        ExecutorService pool = Executors.newFixedThreadPool(3);
        ResponseTimes.enableGlobalCollection(true);
        ResponseTimes.current().clear();
        ResponseTimes.global().clear();

        //act
        for(int i = 0; i < 3; i++){
            pool.submit(() -> {
                ResponseTimes global = ResponseTimes.global();
                global.stopTx(global.startTx("global"));
                global.stopTx(global.startTx("global"));
                global.stopTx(global.startTx("global"));
            });
        }
        ResponseTimes current = ResponseTimes.current();
        current.stopTx(current.startTx("local"));
        current.stopTx(current.startTx("local"));
        current.stopTx(current.startTx("local"));
        pool.shutdown();
        pool.awaitTermination(250000, TimeUnit.MILLISECONDS);

        assertEquals(2, ResponseTimes.global().getResponseTimes().size());
        assertEquals(9, ResponseTimes.global().getResponseTimes().get("global").size());

    }
}

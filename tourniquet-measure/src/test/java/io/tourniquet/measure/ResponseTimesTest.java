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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeFalse;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 *
 */
public class ResponseTimesTest {

    /**
     * The class under test
     */
    @InjectMocks
    private ResponseTimes subject = ResponseTimes.current();

    @After
    public void tearDown() throws Exception {
        subject.clear();
        subject.onMeasureStart(null);
        subject.onMeasureEnd(null);
    }

    @Test
    public void testClear() throws Exception {
        //prepare
        subject.collect(new ResponseTime("test", Instant.now(), Duration.ofMillis(100)));
        assumeFalse(subject.getResponseTimes().isEmpty());

        //act
        subject.clear();

        //assert
        assertTrue(subject.getResponseTimes().isEmpty());

    }

    @Test
    public void testOnMeasureStart() throws Exception {
        //prepare
        AtomicReference<ResponseTime> rtRef = new AtomicReference<>();

        //act
        subject.onMeasureStart(rtRef::set);

        //assert
        subject.startTx("test", Instant.now());
        assertNotNull(rtRef.get());

    }

    @Test
    public void testOnMeasureEnd() throws Exception {
        //prepare
        AtomicReference<ResponseTime> rtRef = new AtomicReference<>();

        //act
        subject.onMeasureEnd(rtRef::set);

        //assert
        subject.stopTx(subject.startTx("test", Instant.now()));
        assertNotNull(rtRef.get());
    }

    @Test
    public void testStartTx_Instant() throws Exception {
        //prepare
        Instant now = Instant.now();

        //act
        ResponseTime rt = subject.startTx("test", now);

        //assert
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
        assertEquals(now, rt.getStart());
    }

    @Test
    public void testStartTx() throws Exception {
        //prepare

        //act
        ResponseTime rt = subject.startTx("test");

        //assert
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
    }

    @Test
    public void testStopTx_finishedResponseTime() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now(), Duration.ofMillis(100));

        //act
        ResponseTime actual = subject.stopTx(rt);

        //assert
        assertNotNull(actual);
        assertEquals(rt, actual);
    }

    @Test
    public void testStopTx_unfinishedResponseTime() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now());
        assumeFalse(rt.isFinished());

        //act
        ResponseTime actual = subject.stopTx(rt);

        //assert
        assertNotNull(actual);
        assertEquals(rt, actual);
        assertTrue(actual.isFinished());
    }

    @Test(expected = AssertionError.class)
    public void testCollect_unfinished_exception() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now());

        //act
        subject.collect(rt);

    }

    @Test
    public void testCollect_finished() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now(), Duration.ofMillis(100));

        //act
        ResponseTime actual = subject.collect(rt);

        //assert
        assertNotNull(actual);
        assertEquals(rt, actual);
        assertFalse(subject.getResponseTimes().isEmpty());
    }

    @Test
    public void testCollect_txNameAndMeasure() throws Exception {
        //prepare
        Instant now = Instant.now();
        Duration dur = Duration.ofMillis(100);
        SimpleTimeMeasure tm = new SimpleTimeMeasure(now, dur);

        //act
        ResponseTime rt = subject.collect("test", tm);

        //assert
        assertNotNull(rt);
        assertEquals(now,rt.getStart());
        assertEquals(dur,rt.getDuration());
        assertFalse(subject.getResponseTimes().isEmpty());
    }

    @Test(expected = AssertionError.class)
    public void testCollect_txNameAndMeasure_unfinished_exception() throws Exception {
        //prepare
        SimpleTimeMeasure tm = new SimpleTimeMeasure(Instant.now());

        //act
        subject.collect("test", tm);
    }

    @Test
    public void testGetResponseTimes() throws Exception {
        //prepare
        subject.collect("tx1", new SimpleTimeMeasure(Instant.now(), Duration.ZERO));
        subject.collect("tx1", new SimpleTimeMeasure(Instant.now(), Duration.ZERO));
        subject.collect("tx2", new SimpleTimeMeasure(Instant.now(), Duration.ZERO));

        //act
        Map<String, List<ResponseTime>> rts = subject.getResponseTimes();

        //assert
        assertNotNull(rts);
        assertEquals(2, rts.size());
        assertTrue(rts.containsKey("tx1"));
        assertTrue(rts.containsKey("tx2"));
        assertEquals(2, rts.get("tx1").size());
        assertEquals(1, rts.get("tx2").size());
    }
}

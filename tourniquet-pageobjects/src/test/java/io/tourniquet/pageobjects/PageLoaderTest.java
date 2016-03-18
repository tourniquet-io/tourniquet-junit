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
import static org.junit.Assert.assertNotNull;

import java.util.concurrent.atomic.AtomicReference;

import io.tourniquet.measure.ResponseTime;
import io.tourniquet.measure.ResponseTimeCollector;
import io.tourniquet.measure.ResponseTimes;
import io.tourniquet.tx.Transaction;
import io.tourniquet.tx.TransactionSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class PageLoaderTest {

    private ResponseTimeCollector rtc = new ResponseTimeCollector();
    private AtomicReference<ResponseTime> rtEndRef = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {
        ResponseTimes.current().onMeasureEnd(responseTime -> rtEndRef.set(responseTime));
        rtc.startCollecting();
    }

    @After
    public void tearDown() throws Exception {
        rtc.stopCollecting();
        ResponseTimes.current().onMeasureEnd(null);
        ResponseTimes.current().clear();
    }

    @Test
    public void testLoadPage() throws Exception {
        //act
        TestPage page = PageLoader.loadPage(TestPage.class);
        //assert
        assertNotNull(page);
    }

    @Test
    public void testLoadTransactionalPage() throws Exception {
        //act
        TxPage page = PageLoader.loadPage(TxPage.class);
        //assert
        assertNotNull(page);
        page.transactionalOp();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertEquals("transactionalOp", rt.getTransaction());
    }

    @Test(expected = AssertionError.class)
    public void testLoadPage_privateConstructor() throws Exception {
        PageLoader.loadPage(PrivateConstructorPage.class);
    }

    @Test(expected = AssertionError.class)
    public void testLoadPage_parameterConstructor() throws Exception {
        PageLoader.loadPage(ParameterConstructorPage.class);
    }

    public static class TestPage implements Page {}
    public static class ParameterConstructorPage implements Page {
        public ParameterConstructorPage(String someParam){}
    }
    public static class PrivateConstructorPage implements Page {
        private PrivateConstructorPage(){}
    }
    public static class TxPage implements Page, TransactionSupport {
        @Transaction
        public void transactionalOp(){}
    }
}

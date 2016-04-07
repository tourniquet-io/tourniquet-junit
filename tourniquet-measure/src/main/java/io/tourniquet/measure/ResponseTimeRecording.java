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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;

import io.tourniquet.junit.rules.ExternalResource;

/**
 * JUnit Rule to record response times. Use this rule if your page object model uses the transaction support
 * to record response times.
 */
public class ResponseTimeRecording extends ExternalResource {

    private static final Logger LOG = getLogger(ResponseTimeRecording.class);

    private final ResponseTimeCollector collector = new ResponseTimeCollector();

    private boolean clearResponseTimesFlag = true;
    private boolean printTransactionsFlag = true;

    /**
     * Sets whether to reset the response time table of the current thread after the test.
     * Default is true. See {@link ResponseTimes#clear()} for more information.
     * @param clearResponseTimes
     *  true if table should be cleared (default) or false if not
     * @return
     *  this rule
     */
    public ResponseTimeRecording clearResponseTimes(final boolean clearResponseTimes) {

        this.clearResponseTimesFlag = clearResponseTimes;
        return this;
    }

    /**
     * Sets whether to print out the recorded transactions at the end of the test.
     * @param printTransactions
     *  true if the transactions should be displayed (default), false if not
     * @return
     *  this rule
     */
    public ResponseTimeRecording printTransactions(final boolean printTransactions) {

        this.printTransactionsFlag = printTransactions;
        return this;
    }

    @Override
    protected void beforeClass() throws Throwable {
        before();
    }

    @Override
    protected void before() throws Throwable {
        collector.startCollecting();
    }

    @Override
    protected void afterClass() {
        after();
    }

    @Override
    protected void after() {
        collector.stopCollecting();
        if(printTransactionsFlag){
            LOG.info("Listing transactions");
            Map<String, List<ResponseTime>> responseTimes = ResponseTimes.current().getResponseTimes();
            for(Map.Entry<String, List<ResponseTime>> e : responseTimes.entrySet()){
                LOG.info("Transaction {}", e.getKey());
                for(ResponseTime rt : e.getValue()){
                    LOG.info("\tstart={} duration={}", rt.getStart(), rt.getDuration());
                }
            }
        }
        if(clearResponseTimesFlag) {
            ResponseTimes.current().clear();
        }
    }

}

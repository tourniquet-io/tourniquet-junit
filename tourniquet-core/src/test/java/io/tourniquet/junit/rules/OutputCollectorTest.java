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

package io.tourniquet.junit.rules;

import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import io.tourniquet.junit.util.TestExecutionContext;
import org.junit.Test;

/**
 *
 */
public class OutputCollectorTest {

    /**
     * The class under test
     */
    private OutputCollector subject = new OutputCollector();

    @Test
    public void testUseConsumer() throws Exception {

        //prepare
        AtomicReference<String> key = new AtomicReference<>();
        AtomicReference<String> value = new AtomicReference<>();

        //act
        subject.useConsumer((k,v) -> {
            key.set(k);
            value.set(v);
        });

        //assert
        subject.setOutput("key", "value");
        assertEquals("key", key.get());
        assertEquals("value", value.get());

    }

    @Test
    public void testSetOutput_defaultConsumer_testContext() throws Exception {

        //prepare
        TestExecutionContext.init(new Properties(), new Properties());

        //act
        subject.setOutput("test", "value");

        //assert
        Properties output = TestExecutionContext.destroy();
        assertEquals("value", output.getProperty("test"));

    }
}

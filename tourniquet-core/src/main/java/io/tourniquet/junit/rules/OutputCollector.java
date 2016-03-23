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

import java.util.function.BiConsumer;

import io.tourniquet.junit.util.TestExecutionContext;

/**
 * The output collector may be used to process output generated by a test. The default behavior is to forward
 * the output to the current {@link io.tourniquet.junit.util.TestExecutionContext} if it is initialized, but
 * other consumers can be defined as well.
 */
public class OutputCollector extends BaseRule {

    private BiConsumer<String, String> consumer = defaultConsumer();

    public OutputCollector useConsumer(BiConsumer<String, String> consumer) {
        this.consumer = consumer;
        return this;
    }

    /**
     * Collect the provided output. Note that outputs with same keys override each other.
     * @param key
     *  the key for the recorded output
     * @param value
     *  the value
     */
    public void setOutput(String key, String value){
        consumer.accept(key, value);
    }

    /*
     * Factory method for improved readability
     */
    private static BiConsumer<String, String> defaultConsumer() {
        return (key, value) -> TestExecutionContext.current()
                                                   .map(TestExecutionContext::getOutput)
                                                   .ifPresent(output -> output.setProperty(key, value));
    }

}
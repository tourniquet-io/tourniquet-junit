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

import java.util.Optional;
import java.util.Properties;

/**
 * The test context allows to define a set of properties that are valid only for the current thread.
 */
public class TestExecutionContext {

    private static final ThreadLocal<TestExecutionContext> CURRENT = new ThreadLocal<>();

    private final Properties properties;

    TestExecutionContext(final Properties properties) {
        this.properties = properties;
    }

    public static Optional<TestExecutionContext> current(){
        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Initializes the test context with the provided set of properties
     *
     * @param props
     *         the properties to initialize the context
     */
    public static void init(Properties props) {

        CURRENT.set(new TestExecutionContext(props));
    }

    /**
     * Destroys the test context for the current thread.
     */
    public static void destroy(){
        CURRENT.remove();
    }

    /**
     * Retrieves the properties of the current test context or throws an {@link java.lang.IllegalStateException} if no
     * context has been defined.
     *
     * @return the properties of the current test context
     */
    public Properties getProperties() {
        return properties;
    }

}

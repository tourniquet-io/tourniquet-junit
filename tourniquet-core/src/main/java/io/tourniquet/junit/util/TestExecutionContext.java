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
 * Provides a context for a test execution that allows to
 * <ul>
 *     <li>Set the system properties for the test execution</li>
 *     <li>Specify input parameters for a test</li>
 *     <li>Collect and retrieve output parameters produced by the test</li>
 * </ul>
 * To get convient access to this context, you may use the rules
 * <ul>
 *     <li>ParameterProvider - which obtains the provided parameters from this context (as default)</li>
 *     <li>OutputCollector - which stores collected outputs in this context (as default)</li>
 * </ul>
 * <br>
 * To make use of this execution context, test may be executed using the {@link JUnitRunner}.
 */
public class TestExecutionContext {

    private static final ThreadLocal<TestExecutionContext> CURRENT = new ThreadLocal<>();

    private final Properties input;
    private final Properties output;
    private final Properties env;

    TestExecutionContext(final Properties input, final Properties env) {

        this.env = env;
        this.input = input;
        this.output = new Properties();
    }

    public static Optional<TestExecutionContext> current() {

        return Optional.ofNullable(CURRENT.get());
    }

    /**
     * Initializes the test context with the provided set of properties
     *
     * @param input
     *         the input parameters for the test
     * @param env
     *         the environment parameters for the test execution.
     */
    public static void init(Properties input, Properties env) {

        CURRENT.set(new TestExecutionContext(input, env));
    }

    /**
     * Destroys the test context for the current thread.
     */
    public static Properties destroy() {

        final Properties output = current().map(TestExecutionContext::getOutput)
                                     .orElseThrow(() -> new IllegalStateException("Context not initialized"));
        CURRENT.remove();
        return output;
    }

    /**
     * Retrieves the environment of the current test context. Note, the returned properties are a clone of the original,
     * therefore changing values won't have any effects.
     *
     * @return the env properties of the current test context
     */
    public Properties getEnv() {

        return (Properties) env.clone();
    }

    /**
     * Returns access to the input properties. Note, the returned properties are a clone of the original, therefore
     * changing values won't have any effects.
     *
     * @return a copy of the input parameters
     */
    public Properties getInput() {

        return (Properties) input.clone();
    }

    /**
     * Provides access to the output properties field. Added or modified values on the properties are stored in the test
     * context. On destroying the context, these properties are returned.
     *
     * @return the properties to be returned from the test context
     */
    public Properties getOutput() {

        return output;
    }

}

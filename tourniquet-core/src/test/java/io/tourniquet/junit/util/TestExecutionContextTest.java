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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import java.util.Properties;

import org.junit.After;
import org.junit.Test;

/**
 *
 */
public class TestExecutionContextTest {

    @After
    public void tearDown() throws Exception {

        TestExecutionContext.current().ifPresent( ctx ->
            TestExecutionContext.destroy()
        );
    }

    @Test
    public void testCurrent_notInitialized_empty() throws Exception {
        //prepare

        //act
        Optional<TestExecutionContext> current = TestExecutionContext.current();

        //assert
        assertNotNull(current);
        assertFalse(current.isPresent());
    }

    @Test(expected = IllegalStateException.class)
    public void testDestroy_not_initialized_exception() throws Exception {

        Properties props = TestExecutionContext.destroy();
        //assert
        assertNotNull(props);
        assertTrue(props.isEmpty());

    }

    @Test
    public void testCurrent_initialized() throws Exception {
        //prepare
        TestExecutionContext.init(new Properties(), new Properties());

        //act
        Optional<TestExecutionContext> current = TestExecutionContext.current();

        //assert
        assertNotNull(current);
        assertTrue(current.isPresent());
    }

    @Test
    public void testInit_Destroy() throws Exception {

        //prepare
        final Properties env = new Properties();
        final Properties input = new Properties();

        //act
        Properties output;
        try {
            TestExecutionContext.init(input, env);
            Properties outputProps = TestExecutionContext.current().get().getOutput();
            outputProps.setProperty("output", "outValue");
        } finally {
            output = TestExecutionContext.destroy();
        }

        //assert
        assertFalse(TestExecutionContext.current().isPresent());
        assertEquals("outValue", output.getProperty("output"));

    }

    @Test
    public void testImmutableInput() throws Exception {

        //prepare
        final Properties env = new Properties();
        final Properties input = new Properties();
        input.setProperty("input", "anInput");

        //act
        Properties output;
        try {
            TestExecutionContext.init(input, env);
            Properties testInput = TestExecutionContext.current().get().getInput();
            assertEquals("anInput", testInput.getProperty("input"));
            testInput.setProperty("modified", "value");
        } finally {
            output = TestExecutionContext.destroy();
        }

        //assert
        assertFalse(TestExecutionContext.current().isPresent());
        assertFalse(input.containsKey("modified"));
    }

    @Test
    public void testImmutableEnv() throws Exception {

        //prepare
        final Properties env = new Properties();
        env.setProperty("param", "value");
        final Properties input = new Properties();

        //act
        try {
            TestExecutionContext.init(input, env);
            Properties testEnv= TestExecutionContext.current().get().getEnv();
            assertEquals("value", testEnv.getProperty("param"));
            testEnv.setProperty("modified", "value");
        } finally {
            TestExecutionContext.destroy();
        }

        //assert
        assertFalse(TestExecutionContext.current().isPresent());
        assertFalse(env.containsKey("modified"));
    }


}

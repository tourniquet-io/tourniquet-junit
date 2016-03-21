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
        TestExecutionContext.destroy();
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

    @Test
    public void testCurrent_initialized() throws Exception {
        //prepare
        TestExecutionContext.init(new Properties());

        //act
        Optional<TestExecutionContext> current = TestExecutionContext.current();

        //assert
        assertNotNull(current);
        assertTrue(current.isPresent());
    }

    @Test
    public void testInit_Destroy() throws Exception {

        //prepare
        Properties props = new Properties();
        props.setProperty("test", "value");

        //act
        try {
            TestExecutionContext.init(props);
            Properties testProps = TestExecutionContext.current().get().getProperties();
            assertEquals("value", testProps.getProperty("test"));
            testProps.setProperty("test2", "value2");
        } finally {
            TestExecutionContext.destroy();
        }

        //assert
        assertFalse(TestExecutionContext.current().isPresent());
        assertEquals("value2", props.getProperty("test2"));

    }

}

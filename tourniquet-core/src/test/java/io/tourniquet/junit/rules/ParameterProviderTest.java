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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Optional;
import java.util.Properties;

import io.tourniquet.junit.util.TestExecutionContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ParameterProviderTest {

    @Rule
    public SystemProperties sysprops = new SystemProperties();

    /**
     * The class under test
     */
    private ParameterProvider subject = new ParameterProvider();

    @Mock
    private Description description;

    @Test
    public void testUseProvider() throws Exception {

        //prepare
        subject.useProvider(key -> Optional.of(key.toUpperCase()));

        //act
        Optional<String> result = subject.getValue("mykey", String.class);

        //assert
        assertEquals("MYKEY", result.get());

    }

    @Test
    public void testGetValue_existing_converted() throws Exception {
        //prepare
        System.setProperty("testParamInt", "123");

        //act
        int value = subject.getValue("testParamInt", Integer.class, 456);

        //assert
        assertEquals(123, value);
    }

    @Test
    public void testGetValue_nonExisting_default() throws Exception {
        //prepare

        //act
        int value = subject.getValue("testParamInt", int.class, 456);

        //assert
        assertEquals(456, value);
    }

    @Test
    public void testGetValueOptional_existing_converted() throws Exception {
        //prepare
        System.setProperty("testParamInt", "123");

        //act
        Optional<Integer> value = subject.getValue("testParamInt", Integer.class);

        //assert
        assertNotNull(value);
        assertEquals(Integer.valueOf(123), value.get());
    }

    @Test
    public void testGetValueOptional_nonExisting_default() throws Exception {
        //prepare

        //act
        Optional<Integer> value = subject.getValue("testParamInt", Integer.class);

        //assert
        assertNotNull(value);
        assertFalse(value.isPresent());
    }

    @Test
    public void testGetValueString_existing_converted() throws Exception {
        //prepare
        System.setProperty("testParamInt", "123");

        //act
        Optional<String> value = subject.getValue("testParamInt");

        //assert
        assertNotNull(value);
        assertEquals("123", value.get());
    }

    @Test
    public void testGetValueString_nonExisting_default() throws Exception {
        //prepare

        //act
        Optional<String> value = subject.getValue("testParamInt");

        //assert
        assertNotNull(value);
        assertFalse(value.isPresent());
    }

    @Test
    public void testDataProvisionWithTestExecutionContext() throws Throwable {
        //prepare
        final Properties env = new Properties();
        final Properties input = new Properties();
        ParameterProvider provider = new ParameterProvider();
        input.put("testParam", "testValue");

        //act
        Properties output;
        try {
            TestExecutionContext.init(input, env);
            provider.apply(new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    assertEquals("testValue", provider.getValue("testParam").get());
                    TestExecutionContext.current()
                                        .map(TestExecutionContext::getOutput)
                                        .ifPresent(p -> p.put("testResult", "result"));
                }
            }, description).evaluate();

        } finally {
            output = TestExecutionContext.destroy();
        }

        //assert
        assertEquals("result", output.getProperty("testResult"));

    }
}

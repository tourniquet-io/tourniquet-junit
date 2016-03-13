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
import static org.junit.Assert.assertNull;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 18.11.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class SystemPropertiesTest  {

    public static final String A_TEST_PROPERTY = "a_test_property";
    /**
     * The class under test
     */
    @InjectMocks
    private SystemProperties subject;

    @Mock
    private Description description;

    @Test
    public void testApply() throws Throwable {

        //prepare
        assumeTrue(System.getProperty(A_TEST_PROPERTY) == null);
        Statement stmt = new Statement() {

            @Override
            public void evaluate() throws Throwable {
                System.setProperty(A_TEST_PROPERTY, "someValue");
                assertEquals("someValue", System.getProperty(A_TEST_PROPERTY) );
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertNull(System.getProperty(A_TEST_PROPERTY));
    }
}

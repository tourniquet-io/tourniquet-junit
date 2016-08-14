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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 */
public class TypeUtilsTest {

    @Test
    public void testIsAbstract_nonAbstract_false() throws Exception {
        //prepare

        //act
        assertFalse(TypeUtils.isAbstract(ConcreteClass.class));

        //assert

    }

    @Test
    public void testIsAbstract_abstract_true() throws Exception {
        //prepare

        //act
        assertTrue(TypeUtils.isAbstract(AbstractClass.class));

        //assert

    }

    //// Test types

    public static class ConcreteClass{}
    public static abstract class AbstractClass{}

}

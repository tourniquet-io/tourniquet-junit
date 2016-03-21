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

import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 */
public class TypeConverterTest {

    @Test
    public void convertTo_() throws Exception {
        //Arrange
        String input = "true";
        Class type = boolean.class;
        //Act
        Object result = TypeConverter.convert(input).to(type);

        //Assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_boolean() throws Exception {
        //arrange
        String input = "true";
        Class type = boolean.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Boolean() throws Exception {
        //arrange
        String input = "true";
        Class type = Boolean.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_byte() throws Exception {
        //arrange
        String input = "1";
        Class type = byte.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Byte() throws Exception {
        //arrange
        String input = "1";
        Class type = Byte.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_short() throws Exception {
        //arrange
        String input = "1";
        Class type = short.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Short() throws Exception {
        //arrange
        String input = "1";
        Class type = Short.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_int() throws Exception {
        //arrange
        String input = "1";
        Class type = int.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Integer() throws Exception {
        //arrange
        String input = "1";
        Class type = Integer.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_long() throws Exception {
        //arrange
        String input = "1";
        Class type = long.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Long() throws Exception {
        //arrange
        String input = "1";
        Class type = Long.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_float() throws Exception {
        //arrange
        String input = "1";
        Class type = float.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Float() throws Exception {
        //arrange
        String input = "1";
        Class type = Float.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_double() throws Exception {
        //arrange
        String input = "1";
        Class type = double.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_Double() throws Exception {
        //arrange
        String input = "1";
        Class type = Double.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }

    @Test
    public void testConvertTo_String() throws Exception {
        //arrange
        String input = "1";
        Class type = String.class;
        //act
        Object result = TypeConverter.convert(input).to(type);

        //assert
        assertThat(result, isA(type));
    }
}

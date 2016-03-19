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

package io.tourniquet.junit.inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class TypeUtilTest {

    @Test
    public void testPrimitiveTypeFor_Boolean() throws Exception {

        assertEquals(boolean.class, TypeUtil.primitiveTypeFor(Boolean.class));
    }

    @Test
    public void testPrimitiveTypeFor_Byte() throws Exception {

        assertEquals(byte.class, TypeUtil.primitiveTypeFor(Byte.class));
    }

    @Test
    public void testPrimitiveTypeFor_Short() throws Exception {

        assertEquals(short.class, TypeUtil.primitiveTypeFor(Short.class));
    }

    @Test
    public void testPrimitiveTypeFor_Integer() throws Exception {

        assertEquals(int.class, TypeUtil.primitiveTypeFor(Integer.class));
    }

    @Test
    public void testPrimitiveTypeFor_Long() throws Exception {

        assertEquals(long.class, TypeUtil.primitiveTypeFor(Long.class));
    }

    @Test
    public void testPrimitiveTypeFor_Float() throws Exception {

        assertEquals(float.class, TypeUtil.primitiveTypeFor(Float.class));
    }

    @Test
    public void testPrimitiveTypeFor_Double() throws Exception {

        assertEquals(double.class, TypeUtil.primitiveTypeFor(Double.class));
    }

    @Test
    public void testPrimitiveTypeFor_Character() throws Exception {

        assertEquals(char.class, TypeUtil.primitiveTypeFor(Character.class));
    }

    @Test
    public void testObjectTypeFor_boolean() throws Exception {

        assertEquals(Boolean.class, TypeUtil.objectTypeFor(boolean.class));
    }

    @Test
    public void testObjectTypeFor_byte() throws Exception {

        assertEquals(Byte.class, TypeUtil.objectTypeFor(byte.class));
    }

    @Test
    public void testObjectTypeFor_short() throws Exception {

        assertEquals(Short.class, TypeUtil.objectTypeFor(short.class));
    }

    @Test
    public void testObjectTypeFor_int() throws Exception {

        assertEquals(Integer.class, TypeUtil.objectTypeFor(int.class));
    }

    @Test
    public void testObjectTypeFor_long() throws Exception {

        assertEquals(Long.class, TypeUtil.objectTypeFor(long.class));
    }

    @Test
    public void testObjectTypeFor_float() throws Exception {

        assertEquals(Float.class, TypeUtil.objectTypeFor(float.class));
    }

    @Test
    public void testObjectTypeFor_double() throws Exception {

        assertEquals(Double.class, TypeUtil.objectTypeFor(double.class));
    }

    @Test
    public void testObjectTypeFor_char() throws Exception {

        assertEquals(Character.class, TypeUtil.objectTypeFor(char.class));
    }

    @Test
    public void testConvert_NullToNull() throws Exception {

        assertNull(TypeUtil.convert(null, Object.class));

    }

    @Test
    public void testConvert_NullToPrimitiveType() throws Exception {

        assertNull(TypeUtil.convert(null, boolean.class));
        assertNull(TypeUtil.convert(null, char.class));
        assertNull(TypeUtil.convert(null, byte.class));
        assertNull(TypeUtil.convert(null, short.class));
        assertNull(TypeUtil.convert(null, int.class));
        assertNull(TypeUtil.convert(null, long.class));
        assertNull(TypeUtil.convert(null, float.class));
        assertNull(TypeUtil.convert(null, double.class));

    }

    @Test
    public void testConvert_NullToObjectType() throws Exception {

        assertNull(TypeUtil.convert(null, Boolean.class));
        assertNull(TypeUtil.convert(null, Character.class));
        assertNull(TypeUtil.convert(null, Byte.class));
        assertNull(TypeUtil.convert(null, Short.class));
        assertNull(TypeUtil.convert(null, Integer.class));
        assertNull(TypeUtil.convert(null, Long.class));
        assertNull(TypeUtil.convert(null, Float.class));
        assertNull(TypeUtil.convert(null, Double.class));

    }

    @Test
    public void testConvert_AnyTypeToAnyType() throws Exception {

        //prepare
        String o = new String();

        //act
        Object actual = TypeUtil.convert(o, Object.class);

        //assert
        assertEquals(o, actual);
    }

    @Test
    public void testConvert_StringToPrimitiveBoolean() throws Exception {

        assertEquals(true, TypeUtil.convert("true", boolean.class));

    }

    @Test
    public void testConvert_StringToPrimitiveCharacter() throws Exception {

        assertEquals('c', TypeUtil.convert("c", char.class));

    }

    @Test
    public void testConvert_StringToPrimitiveByte() throws Exception {

        assertEquals((byte) 123, TypeUtil.convert("123", byte.class));

    }

    @Test
    public void testConvert_StringToPrimitiveShort() throws Exception {

        assertEquals((short) 123, TypeUtil.convert("123", short.class));

    }

    @Test
    public void testConvert_StringToPrimitiveInteger() throws Exception {

        assertEquals(123, TypeUtil.convert("123", int.class));

    }

    @Test
    public void testConvert_StringToPrimitiveLong() throws Exception {

        assertEquals(123L, TypeUtil.convert("123", long.class));

    }

    @Test
    public void testConvert_StringToPrimitiveFloat() throws Exception {

        assertEquals(1.23f, TypeUtil.convert("1.23", float.class));

    }

    @Test
    public void testConvert_StringToPrimitiveDouble() throws Exception {

        assertEquals(1.23, TypeUtil.convert("1.23", double.class));

    }

    @Test
    public void testConvert_StringToBoolean() throws Exception {

        assertEquals(Boolean.TRUE, TypeUtil.convert("true", Boolean.class));

    }

    @Test
    public void testConvert_StringToCharacter() throws Exception {

        assertEquals(Character.valueOf('c'), TypeUtil.convert("c", Character.class));

    }

    @Test
    public void testConvert_StringToByte() throws Exception {

        assertEquals(Byte.valueOf((byte) 123), TypeUtil.convert("123", Byte.class));

    }

    @Test
    public void testConvert_StringToShort() throws Exception {

        assertEquals(Short.valueOf((short) 123), TypeUtil.convert("123", Short.class));

    }

    @Test
    public void testConvert_StringToInteger() throws Exception {

        assertEquals(Integer.valueOf(123), TypeUtil.convert("123", Integer.class));

    }

    @Test
    public void testConvert_StringToLong() throws Exception {

        assertEquals(Long.valueOf(123L), TypeUtil.convert("123", Long.class));

    }

    @Test
    public void testConvert_StringToFloat() throws Exception {

        assertEquals(Float.valueOf(1.23f), TypeUtil.convert("1.23", Float.class));

    }

    @Test
    public void testConvert_StringToDouble() throws Exception {

        assertEquals(Double.valueOf(1.23), TypeUtil.convert("1.23", Double.class));

    }

}

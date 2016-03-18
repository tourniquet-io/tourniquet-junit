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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for dealing with primitive and object types.
 */
public final class TypeUtil {

    /**
     * Map of primitive wrapper types to the corresponding primitive type.
     */
    private static final Map<Class<?>, Class<?>> OBJECT_TO_PRIMITIVE_TYPE_MAP;
    /**
     * Map of object type wrapper types for the corresponding primitive type.
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_OBJECT_TYPE_MAP;

    static {
        final Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(Boolean.class, boolean.class);
        map.put(Byte.class, byte.class);
        map.put(Short.class, short.class); //NOSONAR
        map.put(Integer.class, int.class);
        map.put(Long.class, long.class);
        map.put(Float.class, float.class);
        map.put(Double.class, double.class);
        map.put(Character.class, char.class);
        OBJECT_TO_PRIMITIVE_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    static {
        final Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(short.class, Short.class); //NOSONAR
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(float.class, Float.class);
        map.put(double.class, Double.class);
        map.put(char.class, Character.class);
        PRIMITIVE_TO_OBJECT_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    private TypeUtil() {

    }

    /**
     * Returns the primitive type class for the given value type.
     *
     * @param valueType
     *         a primitive wrapper type
     *
     * @return the corresponding primitive type or null if no primitive wrapper type was given
     */
    public static Class<?> primitiveTypeFor(final Class<?> valueType) {

        return OBJECT_TO_PRIMITIVE_TYPE_MAP.get(valueType);

    }

    /**
     * Converts the given string representation of a value to an instance of the specified type. Supported types are:
     * <ul> <li>any primitive type</li> <li>any primitive object type (i.e. Integer.class)</li> <li>any type with a
     * constructor accepting a single string argument containing the representation of the type</li> </ul>
     *
     * @param value
     *         the value to be converted. The value may be null.
     * @param type
     *         the target type for the conversion. The type must not be null.
     *
     * @return an instance of the type representing the specified string value. If the value can not be converted, the
     * original value will be returned.
     */
    public static Object convert(final String value, final Class<?> type) {

        final Object result;

        if (value == null) {
            result = null;
        } else if (type.isPrimitive()) {
            result = toPrimitive(value, type);
        } else if (OBJECT_TO_PRIMITIVE_TYPE_MAP.containsKey(type)) {
            result = valueOf(value, type);
        } else {
            result = value;
        }
        return result;
    }

    /**
     * Converts the given value to it's given primitive type.
     *
     * @param value
     *         the value to be converted
     * @param type
     *         a primitive type class (i.e. {@code int.class}) .
     *
     * @return the converted value (will be a wrapper type)
     */
    private static Object toPrimitive(final String value, final Class<?> type) {

        final Class<?> objectType = objectTypeFor(type);
        final Object objectValue = valueOf(value, objectType);
        final Object primitiveValue;

        final String toValueMethodName = type.getSimpleName() + "Value";
        try {
            final Method toValueMethod = objectType.getMethod(toValueMethodName);
            primitiveValue = toValueMethod.invoke(objectValue);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            //this should never happen
            throw new RuntimeException("Can not convert to primitive type", e); //NOSONAR
        }

        return primitiveValue;
    }

    /**
     * Converts the given String value to a object type. The method assumes the object type has a constructor accepting
     * a single String representation of the content.
     *
     * @param objectType
     *         the object type that MUST have a single String constructor, which is the case for any any primitive
     *         type.
     * @param value
     *         the value as a string to be converted
     *
     * @return the converted value
     *
     * @throws RuntimeException
     *         if the object type has no valueOf method or the method can not be invoked.
     */
    private static Object valueOf(final String value, final Class<?> objectType) {

        if (objectType == Character.class) {
            return value.charAt(0);
        }

        try {
            final Constructor<?> constructor = objectType.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            //this should never happen
            throw new RuntimeException( //NOSONAR
                                        "Could not create instance of type "
                                                + objectType.getName()
                                                + " from value "
                                                + value, e);
        }
    }

    /**
     * Returns the object type class for a given primitive value type.
     *
     * @param primitiveType
     *         the primitive type
     *
     * @return the object type for the given primitive type or null if no primitive type has been passed
     */
    public static Class<?> objectTypeFor(final Class<?> primitiveType) {

        return PRIMITIVE_TO_OBJECT_TYPE_MAP.get(primitiveType);
    }
}

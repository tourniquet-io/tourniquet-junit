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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.tourniquet.junit.UncheckedException;

/**
 * Utilities for dealing with classes.
 */
public final class TypeUtils {

    private TypeUtils(){}

    /**
     * Checks if the specified class denotes an abstract class.
     * @param type
     *  the type class to check
     * @param <T>
     *  the type of the class
     * @return
     *  <code>true</code> if the specified type denotes an abstract type
     */
    public static <T> boolean isAbstract(final Class<T> type) {

        return Modifier.isAbstract(type.getModifiers());
    }

    /**
     * Creates a new type converter for the given value.
     *
     * @param value
     *         the value to be converted
     *
     * @return a new type converter
     */
    public static TypeConverter convert(String value) {
        return new TypeConverter(value);
    }

    public static final class TypeConverter {

        private static final Map<Class, Class> PRIMITIVE_TO_OBJECT_TYPE_MAP;

        static {
            final Map<Class, Class> map = new HashMap<>();
            map.put(boolean.class, Boolean.class);
            map.put(byte.class, Byte.class);
            map.put(short.class, Short.class);
            map.put(int.class, Integer.class);
            map.put(long.class, Long.class);
            map.put(float.class, Float.class);
            map.put(double.class, Double.class);
            PRIMITIVE_TO_OBJECT_TYPE_MAP = Collections.unmodifiableMap(map);
        }

        /**
         * The value to be converted
         */
        private final String value;

        private TypeConverter(String value) {

            this.value = value;
        }

        /**
         * Converts the underlying string value to a primitive value.
         *
         * @param targetType
         *         the type into which the string value should be converted.
         *
         *  @param <T>
         *       the type of the target type
         *
         * @return the converted value.
         */
        public <T> T to(Class<T> targetType) {
            if (targetType == String.class) {
                return (T) value;
            }
            try {
                final String methodName;
                final Class type;
                if (targetType.isPrimitive()) {
                    final String typeName = targetType.getSimpleName();
                    methodName = "parse" + typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
                    type = PRIMITIVE_TO_OBJECT_TYPE_MAP.get(targetType);
                } else {
                    methodName = "valueOf";
                    type = targetType;
                }
                return (T) type.getMethod(methodName, String.class).invoke(null, value);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new UncheckedException("Could not convert value '" + value + "' to type " + targetType.getName(), e);
            }
        }
    }

}

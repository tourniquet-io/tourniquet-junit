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

package io.tourniquet.pageobjects;

import java.lang.reflect.Modifier;

/**
 * Utilities for dealing with classes.
 */
public final class TypeUtils {

    private TypeUtils(){}

    /**
     * Checks if the specified class denotes an abstract class.
     * @param pageType
     *  the type class to check
     * @param <T>
     *  the type of the class
     * @return
     *  <code>true</code> if the specified type denotes an abstract page type
     */
    public static <T> boolean isAbstract(final Class<T> pageType) {

        return Modifier.isAbstract(pageType.getModifiers());
    }

}

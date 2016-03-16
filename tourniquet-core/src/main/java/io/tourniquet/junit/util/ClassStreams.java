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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utility class for dealing with classes and streams
 */
public final class ClassStreams {

    private ClassStreams() {
    }

    /**
     * Creates a stream of classes representing the supertypes (excluding Object.class) of the specified type.
     *
     * @param type
     *         the type whose supertypes should be returned as a stream
     *
     * @return stream of the supertypes of the specified class
     */
    public static Stream<Class> supertypes(final Class type) {
        return selfAndSupertypes(type.getSuperclass());
    }

    /**
     * Creates a stream of classes containing the class itself and all its the supertypes (excluding Object.class) of
     * the specified type.
     *
     * @param type
     *         the type whose supertypes should be returned as a stream
     *
     * @return stream of the class itself and its supertypes
     */
    public static Stream<Class> selfAndSupertypes(final Class type) {
        Class current = type;
        final List<Class> result = new ArrayList<>();
        do {
            result.add(current);
            current = current.getSuperclass();
        } while (current != Object.class);
        return result.stream();
    }

}

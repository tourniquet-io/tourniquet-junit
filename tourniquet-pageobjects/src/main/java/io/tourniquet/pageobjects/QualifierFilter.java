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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Filter for Fields that are annotated with at least one of a set of qualifier annotations.
 */
class QualifierFilter<T extends ElementGroup> implements Predicate<Field> {

    private final Class<T> groupType;
    private final Class<? extends Annotation>[] qualifiers;

    public QualifierFilter(final Class<T> groupType, final Class<? extends Annotation>... qualifiers) {

        this.groupType = groupType;
        this.qualifiers = qualifiers;
    }

    @Override
    public boolean test(final Field field) {
        return groupType.isAssignableFrom(field.getType())
                && (qualifiers.length == 0 || Stream.of(qualifiers).anyMatch(q -> field.getAnnotation(q) != null));
    }
}

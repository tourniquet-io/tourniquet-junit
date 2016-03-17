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

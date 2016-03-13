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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.deltaspike.core.api.config.ConfigProperty;

/**
 * Injection support for injecting DeltaSpike {@link ConfigProperty} annotated properties. The property can be of any
 * type and has a name. If the property annotation defines a default value, it is injected if the inject value is
 * <code>null</code>. Note, that this implementation is not thread-safe. As the defaultValue is updated on the last
 * match processing of injections should be done in a sequential way.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald Muecke</a>
 */
public class ConfigPropertyInjection extends CdiInjection {

    /**
     * Set containing types that are provide from string conversion.
     */
    private static final Set<Class> AUTO_CONVERTIBLE_TYPES;

    static {
        Set<Class> types = new HashSet<>();
        types.add(Boolean.class);
        types.add(Character.class);
        types.add(Byte.class);
        types.add(Short.class);
        types.add(Integer.class);
        types.add(Long.class);
        types.add(Float.class);
        types.add(Double.class);
        types.add(boolean.class);
        types.add(char.class);
        types.add(byte.class);
        types.add(short.class); //NOSONAR
        types.add(int.class);
        types.add(long.class);
        types.add(float.class);
        types.add(double.class);
        AUTO_CONVERTIBLE_TYPES = Collections.unmodifiableSet(types);
    }

    /**
     * The name of the {@link ConfigProperty} into which the value should be injected.
     */
    private final transient String configPropertyName;

    /**
     * Constructor for a config property injection, accepting the property name and the value.
     *
     * @param configPropertyName
     *         the name of the {@link ConfigProperty}
     * @param injectedValue
     *         the value to be injected. May be null.
     */
    public ConfigPropertyInjection(final String configPropertyName, final Object injectedValue, Class<? extends
            Annotation>... qualifiers) {

        super(injectedValue, qualifiers);
        this.configPropertyName = configPropertyName;
    }


    @Override
    protected Object getDefaultValue(final Field field) {
        final ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
        if (configProperty != null && this.configPropertyName.equals(configProperty.name())) {
            return configProperty.defaultValue();
        }
        return super.getDefaultValue(field);
    }

    @Override
    protected void inject(final Object target, final Field field, final Object value) throws IllegalAccessException {

        final Object injectedValue;
        if (this.isAutoConvertible(field, value)) {
            injectedValue = TypeUtil.convert((String) value, field.getType());
        } else {
            injectedValue = value;
        }

        super.inject(target, field, injectedValue);
    }

    @Override
    protected boolean isFieldCandidate(final Field field, final Object injectedValue) {

        //@formatter:off
        return field.getAnnotation(ConfigProperty.class) != null
                && (injectedValue == null
                || super.isFieldCandidate(field, injectedValue)
                || this.isAutoConvertible(field, injectedValue));
        //@formatter:on
    }

    /**
     * Verifies if the injected value can be automatically converted into the field's type.
     *
     * @param field
     *         the target field into which the value should be injected
     * @param injectedValue
     *         the value to be injected
     *
     * @return <code>true</code> if the value can be converted into the field's type
     */
    private boolean isAutoConvertible(final Field field, final Object injectedValue) {

        return injectedValue instanceof String && this.isAutoConvertibleType(field);
    }

    /**
     * Verifies if the field is of a type that provides conversion a method.
     *
     * @param field
     *         the field to check
     *
     * @return <code>true</code> if the field can be automatically converted
     */
    private boolean isAutoConvertibleType(final Field field) {

        return AUTO_CONVERTIBLE_TYPES.contains(field.getType());
    }

    /**
     * Verifies the name of the {@link ConfigProperty}. If the field type matches and the {@link ConfigProperty}'s name
     * matches, the method returns <code>true</code>. If the {@link ConfigProperty} has a default value set, the value
     * will be used in case the injection value is <code>null</code>.
     */
    @Override
    protected boolean isMatching(final Field field) {

        if (!this.isAutoConvertible(field, super.getValue()) && !super.isMatching(field)) {
            return false;
        }
        final ConfigProperty configProperty = field.getAnnotation(ConfigProperty.class);
        if (configProperty != null && this.configPropertyName.equals(configProperty.name())) {
            return true;
        }
        return false;
    }

}

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

import static io.tourniquet.junit.inject.TypeUtil.primitiveTypeFor;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.apache.deltaspike.core.api.config.ConfigProperty;

/**
 * Provides means to inject a value into an arbitrary object's fields.
 * <br>
 * Example:
 * <pre><code>
 *     class Target {
 *         {@literal @}Resource(name=&quot;resourceName&quot;)
 *         private Object myResource
 *         //...
 *     };
 *     //...
 *     Target target = ...;
 *     Object anyValue = ...;
 *     new Injection(anyValue).asResource().byName("resourceName").into(target);
 * </code></pre>
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public class Injection {

    private final Object value;

    /**
     * Creates a new Injection helper for the target object and the object to be injected.
     *
     * @param injectedValue
     *            the object to be injected
     */
    public Injection(final Object injectedValue) {

        if(injectedValue instanceof InjectableHolder){
            this.value = ((InjectableHolder) injectedValue).getInjectionValue();
        } else {
            this.value = injectedValue;
        }
    }

    /**
     * Prepares an injection of a {@link Resource} annotated field.
     *
     * @return a {@link ResourceInjection} handle
     */
    public ResourceInjection asResource() {

        return new ResourceInjection(this.getValue());
    }

    /**
     * The value that should be injected into the specified field
     * @return The value that should be injected.
     */
    protected Object getValue() {

        return this.value;
    }
    /**
     * Provides the default value for the specified field.
     * @param field
     *  the field for which the default value should be determined
     * @return The value that should be injected. Default value is null;
     */
    protected Object getDefaultValue(final Field field) {

        return null;
    }

    /**
     * Determines the effective value for the injection into the specified field. If no value has been set,
     * the default value is returned, which defaults to null but can be overrided with the method
     * {@link #getDefaultValue(java.lang.reflect.Field)}
     * @param field
     *  the field for which the injection value should be retrieved
     * @return
     *  the value to be injected
     */
    private Object getValue(final Field field) {

        final Object val = this.getValue();
        if(val == null) {
            return getDefaultValue(field);
        }
        return val;
    }

    /**
     * Prepares an injection of a {@link ConfigProperty} qualified injection point field.
     *
     * @return a {@link ConfigPropertyInjection} handle
     */
    public ConfigPropertyInjection asConfigProperty(final String propertyName) {

        return new ConfigPropertyInjection(propertyName, this.getValue());
    }

    /**
     * Prepares an injection of {@link javax.inject.Inject} annotated field. The default injection only checks
     * for type compatibility, using this method mandates the presence of the {@code @Inject} annotation.
     @return a {@link CdiInjection} handle
     */
    public CdiInjection asQualifyingInstance(Class<? extends Annotation>... qualifiers){

        return new CdiInjection(this.getValue(), qualifiers);
    }

    /**
     * Injects the value of the injection operation into the first matching field of the target object.
     *
     * @param target
     *            the object into which the injection value is injected
     */
    public void into(final Object target) {

        assertNotNull("Injection target must not be null", target);
        this.injectInto(target, true);
    }

    /**
     * Injects the value of the Injection into the target.
     *
     * @param target
     *            the target for the injection operation
     * @param oneMatch
     *            if set to <code>true</code>, the method returns after one injection operation has been performed.
     *            If set to <code>false</code> the injection value is injected into all matching fields.
     * @throws AssertionError
     *  if the value could not be inject because the field is not accessible.
     */
    private void injectInto(final Object target, final boolean oneMatch) throws AssertionError {

        boolean success = false;

        for (final Field field : this.collectFieldCandidates(target)) {
            if (this.isMatching(field)) {
                Object val = getValue(field);
                try {
                    field.setAccessible(true);
                    this.inject(target, field, val);
                    success = true;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new AssertionError("Injection of " + val + " into " + target + " failed", e);
                }
                if (oneMatch) {
                    return;
                }
            }
        }
        assertTrue("No matching field for injection found", success);
    }

    /**
     * Determines all fields that are candidates for the injection as their type is compatible with the injected object.
     * The method checks the target objects type and supertypes for declared fields.
     *
     * @param target
     *            the target object for which the candidate fields should be determined
     * @return an array of all fields into which the injection object can be injected.
     */
    private List<Field> collectFieldCandidates(final Object target) {

        final Class<?> targetClass = target.getClass();
        return this.collectFieldCandidates(this.value, targetClass);
    }

    /**
     * Determines if the field matches the value considering all specified match criteria. The default implementation
     * only checks for the type compatibility, override for more detailed match verification.
     *
     * @param field
     *            the field to check
     * @return <code>true</code> if the field matches
     */
    protected boolean isMatching(final Field field) {

        return this.isNullOrMatchingType(this.value, field);
    }

    /**
     * Performs the injection. Default implementation simply calls {@code set()} method on the field. Override to
     * provide additional function or preparation.
     *
     * @param target
     *         the target object, holder of the field
     * @param field
     *         the field into which the value should be injected
     * @param value
     *         the value to be injected
     *
     * @throws IllegalAccessException
     *         if the field is not accessible
     */
    protected void inject(final Object target, final Field field, Object value) throws IllegalAccessException {

        field.set(target, value);
    }

    /**
     * Collects all matching declared fields of the target class and returns the result as a list.
     *
     * @param injectedValue
     *         the value that should be injected
     * @param targetClass
     *         the class of the target of the injection whose declared fields should be collected
     *
     * @return a list of fields that are type-compatible with the injected class.
     */
    private List<Field> collectFieldCandidates(final Object injectedValue, final Class<?> targetClass) {

        final List<Field> fieldCandidates = new ArrayList<>();
        Class<?> current = targetClass;
        while (current != Object.class) {

            fieldCandidates.addAll(this.collectionDeclaredFieldCandidates(injectedValue, current));
            current = current.getSuperclass();
        }
        return fieldCandidates;
    }

    /**
     * Checks if the specified value is either null or compatible with the field. Compatibility is verified based
     * on inheritance or primitive-type compatibility.
     * @param injectedValue
     *  the value that is checked against the field
     * @param field
     *  the field that should be compatible with the value
     * @return
     *  <code>true</code> if the value is compatible with the field
     */
    private boolean isNullOrMatchingType(final Object injectedValue, final Field field) {

        if (injectedValue == null) {
            // null is always type-compatible
            return true;
        }

        final Class<?> fieldType = field.getType();
        final Class<?> valueType = injectedValue.getClass();

        if (fieldType.isPrimitive()) {
            return fieldType == primitiveTypeFor(valueType);
        }
        return fieldType.isAssignableFrom(valueType);
    }

    /**
     * Collects all declared fields from the targetClass that are type-compatible with the class of the
     * injected value into the fieldCandidates list.
     *
     * @param injectedValue
     *            the value that should be injected
     * @param targetClass
     *            the class or any of its superclasses of the injection target
     * @return list of declared {@link Field}s that are type-compatible with the injected class
     */
    private List<Field> collectionDeclaredFieldCandidates(final Object injectedValue, final Class<?> targetClass) {
        final List<Field> fieldCandidates = new ArrayList<>();
        for (final Field field : targetClass.getDeclaredFields()) {
            if (this.isFieldCandidate(field, injectedValue)) {
                fieldCandidates.add(field);
            }
        }
        return fieldCandidates;
    }

    /**
     * Checks if a field is an injection candidate for the given injected value.
     *
     * @param field
     *         the field that is a potential candidate
     * @param injectedValue
     *         the value that should be injected. May be null.
     *
     * @return <code>true</code> if the field is a candidate. This does not mean, that the injection will actually be
     * performed into that field.
     */
    protected boolean isFieldCandidate(final Field field, final Object injectedValue) {

        return this.isNullOrMatchingType(injectedValue, field);
    }

    /**
     * Injects the value of the injection operation into all matching fields of the target object.
     *
     * @param target
     *            the object into which the injection value is injected
     */
    public void intoAll(final Object target) {

        this.injectInto(target, false);
    }

}

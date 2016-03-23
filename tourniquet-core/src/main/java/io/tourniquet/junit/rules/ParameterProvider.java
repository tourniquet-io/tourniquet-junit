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

package io.tourniquet.junit.rules;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import io.tourniquet.junit.util.TestExecutionContext;
import io.tourniquet.junit.util.TypeConverter;

/**
 * Rule for accessing test parameters. Test parameters allow to configure a test with external parameters. The default
 * parameter provider is the {@link io.tourniquet.junit.util.TestExecutionContext} with a fallback to the system
 * properties, if the test context is not initialized.
 */
public class ParameterProvider extends BaseRule {

    private Function<String, Optional<String>> provider;

    public ParameterProvider() {

        this.provider = key -> Optional.ofNullable(TestExecutionContext.current()
                                                                       .map(TestExecutionContext::getInput)
                                                                       .orElse(System.getProperties())
                                                                       .getProperty(key));
    }

    /**
     * Sets a parameter provider to be used in the test.
     *
     * @param provider
     *         the provider to be used to resolve parameters. The provider maps the key to a value, while the value is
     *         optional, which means, in case the provider can not resolve the key, it must return the empty optional.
     *         The result of the mapping must be a string representation of the data. Primitive type can be converted
     *         automatically.
     *
     * @return this rule
     */
    public ParameterProvider useProvider(Function<String, Optional<String>> provider) {

        Objects.requireNonNull(provider, "Parameter provider must not be null");
        this.provider = provider;
        return this;
    }

    /**
     * Retrieves a parameter from the parameter provider set for this rule (or the default provider).
     *
     * @param key
     *         the key of the parameter used to identify the parameter value
     * @param type
     *         the type of the parameter value
     * @param defaultValue
     *         the default value to be used in case the provider does not provide a value for the specified key.
     * @param <T>
     *         the type of the parameter value. The method performs an auto-conversion of primitive types, their Object
     *         representation and Strings (no conversion).
     *
     * @return the parameter value
     */
    public <T> T getValue(String key, Class<T> type, T defaultValue) {

        return getValue(key, type).orElse(defaultValue);
    }

    /**
     * Retrieves a parameter from the parameter provider set for this rule (or the default provider). If the parameter
     * is not set, an empty optional is returned.
     *
     * @param key
     *         the key of the parameter used to identify the parameter value
     * @param type
     *         the type of the parameter value
     * @param <T>
     *         the type of the parameter value. The method performs an auto-conversion of primitive types, their Object
     *         representation and Strings (no conversion).
     *
     * @return the parameter value
     */
    public <T> Optional<T> getValue(String key, Class<T> type) {

        return provider.apply(key).map(value -> TypeConverter.convert(value).to(type));
    }

    /**
     * Convenient method for obtaining string valued parameters.
     *
     * @param key
     *         the key of the string parameter
     *
     * @return the string value or empty optional if the parameter is not specified.
     */
    public Optional<String> getValue(String key) {

        return getValue(key, String.class);
    }

}

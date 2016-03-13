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

/**
 *
 */
package io.tourniquet.junit.inject;

/**
 * Interface to be used by Test objects such as {@link org.junit.rules.TestRule}s to provide an object to be injected
 * into a test subject.
 *
 * @param <T>
 *         the type of the injected value
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public interface InjectableHolder<T> {

    /**
     * The value that should be be injected into a injection target.
     *
     * @return the target object to be injected
     */
    public T getInjectionValue();
}

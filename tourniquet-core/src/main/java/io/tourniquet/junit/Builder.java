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

package io.tourniquet.junit;

import org.junit.rules.TestRule;

/**
 * Base class for {@link org.junit.rules.TestRule} builders.
 *
 * @param <T>
 *         the type of the {@link org.junit.rules.TestRule} the build can be used to build
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public interface Builder<T extends TestRule> {

    /**
     * Builds the test rule instance.
     * @return
     *  an instance of the test rule of type T
     */
    T build();

}

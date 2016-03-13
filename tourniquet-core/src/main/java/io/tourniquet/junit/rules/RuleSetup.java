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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This source-only annotation marks a method to be used for setting up a rule and is not intended to be used as public
 * API. The annotation provides a guide for users of the Tourniquet rule API.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface RuleSetup {

    /**
     * Indicates a requirement level of the setup method.
     * 
     * @return
     */
    RequirementLevel value() default RequirementLevel.OPTIONAL;

    /**
     * Indicates the
     *
     * @author Gerald Muecke, gerald@moskito.li
     */
    public static enum RequirementLevel {
        /**
         * The setup method is optional
         */
        OPTIONAL,
        /**
         * The setup method is required
         */
        REQUIRED, ;
    }
}

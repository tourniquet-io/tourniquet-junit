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

/**
 * Helper class to deal with the BaseRule when writing tests for Rules that subclass it
 *
 * @author Gerald Muecke, gerald@moskito.li
 */
public final class BaseRuleHelper {

    private BaseRuleHelper() {

    }

    /**
     * The method sets the state of the rule subject, which can only be done by subclasses or classes in the same
     * package.
     *
     * @param subject
     *         the BaseRule or a subclass of it whose state should be changed
     * @param newState
     *         the new state the rule should be transitioned to
     *
     * @throws Throwable
     */
    public static <T extends BaseRule<?>> void setState(final T subject, BaseRule.State newState) {

        subject.doStateTransition(newState);
    }

    public static <T extends BaseRule<?>> BaseRule.State getState(final T subject) {
        return subject.getCurrentState();
    }

}

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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Alternative for {@link org.junit.rules.ExternalResource} that supports class-level before and after statements and
 * {@link TestRule} chaining.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public abstract class ExternalResource<T extends TestRule> extends BaseRule<T> {

    public ExternalResource() {
        super();
    }

    public ExternalResource(final T outerRule) {
        super(outerRule);
    }

    /**
     * Verifies if the caller is a Suite and triggers the beforeClass and afterClass behavior.
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        if (description.isSuite()) {
            return super.apply(classStatement(base), description);
        }
        return super.apply(statement(base), description);
    }

/**
     * Creates a statement that will execute {@code before) and {@code after)
     *
     * @param base
     *  the base statement to be executed
     * @return
     *  the statement for invoking before and after
     */
    private Statement statement(final Statement base) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                before();
                doStateTransition(State.BEFORE_EXECUTED);
                try {
                    base.evaluate();
                } finally {
                    after();
                    doStateTransition(State.AFTER_EXECUTED);
                }
            }
        };
    }

/**
     * Creates a statement that will execute {@code beforeClass) and {@code afterClass)
     *
     * @param base
     *  the base statement to be executed
     * @return
     *  the statement for invoking beforeClass and afterClass
     */
    private Statement classStatement(final Statement base) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                beforeClass();
                doStateTransition(State.BEFORE_EXECUTED);
                try {
                    base.evaluate();
                } finally {
                    afterClass();
                    doStateTransition(State.AFTER_EXECUTED);
                }
            }
        };
    }

    /**
     * Override to set up your specific external resource for an entire suite.
     *
     * @throws Throwable
     *             if setup fails (which will disable {@code afterClass}
     */
    protected void beforeClass() throws Throwable { // NOSONAR
        // do nothing
    }

    /**
     * Override to tear down your specific external resource after all tests of a suite have been executed.
     */
    protected void afterClass() { //NOSONAR
        // do nothing
    }

    /**
     * Override to set up your specific external resource.
     *
     * @throws Throwable
     *             if setup fails (which will disable {@code after}
     */
    protected void before() throws Throwable { // NOSONAR
        // do nothing
    }

    /**
     * Override to tear down your specific external resource.
     */
    protected void after() { //NOSONAR
        // do nothing
    }
}

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Base {@link TestRule} that allows to define an outer rule that is evaluated around this rule. This is an alternative
 * to {@link RuleChain} that is helpful if rules depend on each other.
 *
 * @author <a href="mailto:gerald.muecke@gmail.com">Gerald M&uuml;cke</a>
 */
public abstract class BaseRule<T extends TestRule> implements TestRule {

    /**
     * Test Rule that should be evaluated around this {@link TestRule}
     */
    private final T outerRule;

    private State currentState = State.NEW;

    /**
     * Creates a rule without an outer rule
     */
    public BaseRule() {
        outerRule = null;
    }

    /**
     * Creates a rule with a that has a rule around it.
     *
     * @param outerRule
     */
    public BaseRule(final T outerRule) {
        this.outerRule = outerRule;
    }

    /**
     * Invokes the outer rule - if set - around the base {@link Statement}
     */
    @Override
    public Statement apply(final Statement base, final Description description) {
        if (outerRule != null) {
            return outerRule.apply(base, description);
        }
        return base;
    }

    /**
     * Returns whether the rule is in the specified state. Unlike the assertion method, this method
     * only checks if the current state is the same as the expected. Note that in some cases it might be more suitable
     * to check if a specific state has been passed, i.e. the State.BEFORE_EXECUTED state implicitly includes the
     * INITIALIZED and CREATED state.
     *
     * @return <code>true</code> when the rule is in the expected, <code>false</code> if not
     */
    protected boolean isInState(State expectedState) {
        return currentState.equals(expectedState);
    }

    /**
     * Checks if the current state of the rule is past the specified state. For example, the state
     * INITIALIZED is past the the state CREATED. If the Rule is in state INITIALIZED, this method would return
     * <code>true</code> for NEW and CREATED and <code>false</code> for INITIALIZED, BEFORE_EXECUTED, AFTER_EXECUTED
     * and DESTROYED.
     * @param passedState
     *  the state the rule should already have passed or be in
     * @return
     *  <code>true</code> if the current state is past or equal the passedState
     */
    protected boolean isAfterState(State passedState) {
        return currentState.compareTo(passedState) > 0;
    }

    /**
     * Checks if the current state of the rule is before the specified state. For example, the state
     * CREATED is before the the state INITIALIZED. If the Rule is in state CREATED, this method would return
     * <code>true</code> for INITIALIZED, BEFORE_EXECUTED, AFTER_EXECUTED
     * and DESTROYED and <code>false</code> for NEW and CREATED.
     * @param futureState
     * @return
     */
    protected boolean isBeforeState(State futureState){
        return currentState.compareTo(futureState) < 0;
    }

    /**
     * Performs a state transition to the new state. There is no check in place, if the transition is valid or not.
     * @param newState
     *  the new state of the rule
     */
    protected void doStateTransition(State newState) {
        this.currentState = newState;
    }

    /**
     * Invoke this method to verify, the rule is exactly in the current state.
     */
    protected void assertStateEquals(State state) {
        assertEquals("Rule is not in the expected state", state, currentState);
    }


    /**
     * Invoke this method to verify, the rule has passed or is in the specified state
     */
    protected void assertStateAfterOrEqual(State state) {
        assertTrue("Rule has not passed the state", isAfterState(state) || isInState(state));
    }

    /**
     * Invoke this method to verify, the rule is before the specified state
     */
    protected void assertStateBefore(State state) {
        assertTrue("Rule has passed the state", isBeforeState(state));
    }

    /**
     * Returns the outer rule for this base rule. The outer rule is applied around the implementing rule
     * 
     * @return the outer rule or <code>null</code> if the rule has no outer rule
     */
    protected T getOuterRule() {
        return this.outerRule;
    }

    /**
     * @return
     *  the current state of the rule
     */
    protected State getCurrentState() {

        return currentState;
    }

    /**
     * States of the rule. The lifecycle of the BaseRule consists of various states.
     */
    public enum State {
        /**
         * State for a newly instantiated rule
         */
        NEW,
        /**
         * State to be used, when the internals of the rules have been created. In most cases this is sufficient to
         * start testing.
         */
        CREATED,
        /**
         * State to be used, when the internals of the rule have been initialized. In some cases it might be required
         * to initialize the internals after they have been created. After this state, the rule should be ready.
         */
        INITIALIZED,
        /**
         * State to indicate the before method has been executed. This state should only be used when the rule
         * is used as instance rule and not as classrule. Reaching this state indicates the instance rule has been
         * set up and is ready for a single test.
         */
        BEFORE_EXECUTED,
        /**
         * State to indicate the after method has been executed. This state should only be used when the rule is
         * used as instance rule and not as classrule. Reaching this state indicates the instance rule has been torn
         * down and has to be re-initialized before reuse. In instance rules, this is the final state.
         */
        AFTER_EXECUTED,
        /**
         * State to indicate the internals of the rule have been destroyed and may not be used unless being
         * re-initialized. In class rules, this is the final state.
         */
        DESTROYED,
        ;
    }
}

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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class BaseRuleTest {

    @Mock
    private TestRule outer;
    @Mock
    private Statement base;
    @Mock
    private Statement statement;
    @Mock
    private Description description;

    private BaseRule<TestRule> subject;

    @Before
    public void setUp() throws Exception {

        subject = new BaseRule<TestRule>() {

        };
    }

    @Test
    public void testBaseRule_withOuterTestRule() throws Exception {

        // prepare
        when(outer.apply(base, description)).thenReturn(statement);
        // act
        final BaseRule<TestRule> subject = new BaseRule<TestRule>(outer) {

        };
        final Statement stmt = subject.apply(base, description);

        // assert
        verify(outer).apply(base, description);
        assertNotNull(stmt);
        assertEquals(statement, stmt);
    }

    @Test
    public void testApply() throws Exception {

        final Statement stmt = subject.apply(base, description);
        assertNotNull(stmt);
        assertEquals(base, stmt);
    }

    @Test
    public void testGetOuterRule() throws Exception {

        final BaseRule<TestRule> subject = new BaseRule<TestRule>(outer) {

        };

        assertEquals(outer, subject.getOuterRule());
    }

    @Test
    public void testIsInState_currentStateBefore_false() throws Exception {
        //prepare

        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        boolean result = subject.isInState(BaseRule.State.INITIALIZED);

        //assert
        assertFalse(result);

    }

    @Test
    public void testIsInState_currentStateEqual_ok() throws Exception {
        //prepare

        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        boolean result = subject.isInState(BaseRule.State.INITIALIZED);

        //assert
        assertTrue(result);

    }

    @Test
    public void testIsInState_currentStateAfter_false() throws Exception {
        //prepare

        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        boolean result = subject.isInState(BaseRule.State.CREATED);

        //assert
        assertFalse(result);

    }

    @Test
    public void testIsAfterState_currentStateBefore_false() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        boolean result = subject.isAfterState(BaseRule.State.INITIALIZED);

        //assert
        assertFalse(result);

    }

    @Test
    public void testIsAfterState_currentStateEquals_false() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        boolean result = subject.isAfterState(BaseRule.State.INITIALIZED);

        //assert
        assertFalse(result);

    }

    @Test
    public void testIsAfterState_currentStateAfter_true() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        boolean result = subject.isAfterState(BaseRule.State.CREATED);

        //assert
        assertTrue(result);

    }

    @Test
    public void testIsBeforeState_currentStateBefore_true() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        boolean result = subject.isBeforeState(BaseRule.State.INITIALIZED);

        //assert
        assertTrue(result);

    }

    @Test
    public void testIsBeforeState_currentStateAfter_false() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        boolean result = subject.isBeforeState(BaseRule.State.CREATED);

        //assert
        assertFalse(result);

    }

    @Test
    public void testIsBeforeState_currentStateEquals_false() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        boolean result = subject.isBeforeState(BaseRule.State.INITIALIZED);

        //assert
        assertFalse(result);

    }

    @Test(expected = AssertionError.class)
    public void testAssertStateEquals_beforeState_fail() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        subject.assertStateEquals(BaseRule.State.NEW);
    }

    @Test
    public void testAssertStateEquals_equalState_ok() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        subject.assertStateEquals(BaseRule.State.CREATED);

        //assert

        //no assertionError should happen, so no need to put any assertion here

    }

    @Test(expected = AssertionError.class)
    public void testAssertStateEquals_afterState_fail() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.NEW);

        //act
        subject.assertStateEquals(BaseRule.State.CREATED);
    }

    @Test(expected = AssertionError.class)
    public void testAssertStateAfterOrEquals_beforeState_fail() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.NEW);

        //act
        subject.assertStateAfterOrEqual(BaseRule.State.CREATED);
    }

    @Test
    public void testAssertStateAfterOrEquals_equalState_ok() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        subject.assertStateAfterOrEqual(BaseRule.State.CREATED);
        //assert
        //no assertionError should happen, so no need to put any assertion here
    }

    @Test
    public void testAssertStateAfterOrEquals_afterState_ok() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        subject.assertStateAfterOrEqual(BaseRule.State.CREATED);
        //assert
        //no assertionError should happen, so no need to put any assertion here
    }

    @Test
    public void testAssertStateBefore_beforeState_ok() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.NEW);

        //act
        subject.assertStateBefore(BaseRule.State.CREATED);
    }

    @Test(expected = AssertionError.class)
    public void testAssertStateBefore_equalState_fail() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.CREATED);

        //act
        subject.assertStateBefore(BaseRule.State.CREATED);
    }

    @Test(expected = AssertionError.class)
    public void testAssertStateBefore_afterState_fail() throws Exception {
        //prepare
        subject.doStateTransition(BaseRule.State.INITIALIZED);

        //act
        subject.assertStateBefore(BaseRule.State.CREATED);
    }

}

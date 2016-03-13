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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ExternalResourceTest {

    @Mock
    private TestRule outer;
    @Mock
    private Statement outerStatement;
    @Mock
    private Statement base;
    @Mock
    private Description description;

    private ExternalResource subject;

    @Before
    public void setUp() throws Exception {

        subject = new ExternalResource() {
        };
        when(outer.apply(base, description)).thenReturn(outerStatement);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testApply_instanceRule() throws Throwable {

        // prepare
        final ExternalResource spy = spy(subject);

        // act
        final Statement stmt = spy.apply(base, description);
        stmt.evaluate();

        // assert
        assertTrue(spy.isAfterState(BaseRule.State.BEFORE_EXECUTED));
        assertTrue(spy.isInState(BaseRule.State.AFTER_EXECUTED));
        verify(spy).before();
        verify(spy).after();
        verify(base).evaluate();
        verify(spy, times(0)).beforeClass();
        verify(spy, times(0)).afterClass();
    }

    @Test
    public void testApply_classRule() throws Throwable {

        // prepare
        final ExternalResource spy = spy(subject);
        when(description.isSuite()).thenReturn(true);

        // act
        final Statement stmt = spy.apply(base, description);
        stmt.evaluate();

        assertTrue(spy.isAfterState(BaseRule.State.BEFORE_EXECUTED));
        assertTrue(spy.isInState(BaseRule.State.AFTER_EXECUTED));
        // assert
        verify(spy, times(0)).before();
        verify(spy, times(0)).after();
        verify(base).evaluate();
        verify(spy).beforeClass();
        verify(spy).afterClass();
    }

    @Test
    public void testExternalResourceTestRule_classeRule() throws Throwable {

        // prepare
        final AtomicBoolean beforeClassInvoked = new AtomicBoolean();
        final AtomicBoolean afterClassInvoked = new AtomicBoolean();
        final ExternalResource spy = spy(new ExternalResource(outer) {

            @Override
            protected void beforeClass() throws Throwable {

                beforeClassInvoked.set(true);
            }

            @Override
            protected void afterClass() {

                afterClassInvoked.set(true);
            }
        });

        when(outer.apply(any(Statement.class), any(Description.class))).thenReturn(outerStatement);
        when(description.isSuite()).thenReturn(true);

        // act
        final Statement stmt = spy.apply(base, description);
        stmt.evaluate();

        // assert
        assertEquals(outerStatement, stmt);
        final ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);
        verify(outer).apply(captor.capture(), any(Description.class));

        // get the statement created by the external rule that is passed to the outer rule
        final Statement internalStatement = captor.getValue();
        internalStatement.evaluate();
        assertTrue(beforeClassInvoked.get());
        assertTrue(afterClassInvoked.get());
    }

    @Test
    public void testExternalResourceTestRule_instanceRule() throws Throwable {

        // prepare
        final AtomicBoolean beforeInvoked = new AtomicBoolean();
        final AtomicBoolean afterInvoked = new AtomicBoolean();
        final ExternalResource spy = spy(new ExternalResource(outer) {

            @Override
            protected void before() throws Throwable {

                beforeInvoked.set(true);
            }

            @Override
            protected void after() {

                afterInvoked.set(true);
            }
        });
        when(outer.apply(any(Statement.class), any(Description.class))).thenReturn(outerStatement);

        // act
        final Statement stmt = spy.apply(base, description);
        stmt.evaluate();

        // assert
        assertEquals(outerStatement, stmt);
        final ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);
        verify(outer).apply(captor.capture(), any(Description.class));

        // get the statement created by the external rule that is passed to the outer rule
        final Statement internalStatement = captor.getValue();
        internalStatement.evaluate();
        assertTrue(beforeInvoked.get());
        assertTrue(afterInvoked.get());

    }

}

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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SystemConsoleTest  {

    private static final String NEW_LINE = System.getProperty("line.separator","\r\n");

    /**
     * The class under test
     */
    @InjectMocks
    private SystemConsole subject;

    @Mock
    private Description description;

    private void assertOut(final String ref) {

        String out = subject.getOut();
        assertNotNull(out);
        assertEquals(ref, out);
        //verify the out stream is reset
        System.out.print("xxx");
        assertEquals(ref, subject.getOut());
    }

    private void assertErr(final String ref) {

        String err = subject.getErr();
        assertNotNull(err);
        assertEquals(ref, err);
        //verify the err stream is reset
        System.err.print("xxx");
        assertEquals(ref, subject.getErr());
    }

    @Test
     public void testApply_print_sysout() throws Throwable {

        //prepare
        final String ref = "truec11.2abcTESTOBJECT";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.out;
                doPrint(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertOut(ref);
    }

    @Test
    public void testApply_println_sysout() throws Throwable {

        //prepare
        final String ref = NEW_LINE //println
                + "true"+NEW_LINE //println boolean
                + "c"+NEW_LINE //println char
                + "1"+NEW_LINE //println int
                + "1.2"+NEW_LINE //println float
                + "abc"+NEW_LINE //println char[]
                + "TEST"+NEW_LINE //println String
                + "OBJECT"+NEW_LINE; //println Object
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.out;
                doPrintln(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertOut(ref);
    }

    @Test
    public void testApply_printf_sysout() throws Throwable {

        //prepare
        final String ref = "f:testF:TEST";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.out;
                doPrintf(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertOut(ref);
    }

    @Test
    public void testApply_format_sysout() throws Throwable {

        //prepare
        final String ref = "f:testF:TEST";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.out;
                doFormat(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertOut(ref);
    }

    @Test
    public void testApply_write_sysout() throws Throwable {

        //prepare
        final String ref = "456789";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.out;
                doWrite(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertOut(ref);
    }

    @Test
    public void testApply_append_sysout() throws Throwable {

        //prepare
        final String ref = "cCSQMIDDLE";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.out;
                doAppend(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();
        //assert
        assertOut(ref);
    }

    @Test
    public void testApply_print_syserr() throws Throwable {

        //prepare
        final String ref = "truec11.2abcTESTOBJECT";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.err;
                doPrint(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertErr(ref);
    }

    @Test
    public void testApply_println_syserr() throws Throwable {

        //prepare
        final String ref = NEW_LINE //println
                + "true"+NEW_LINE //println boolean
                + "c"+NEW_LINE //println char
                + "1"+NEW_LINE //println int
                + "1.2"+NEW_LINE //println float
                + "abc"+NEW_LINE //println char[]
                + "TEST"+NEW_LINE //println String
                + "OBJECT"+NEW_LINE; //println Object
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.err;
                doPrintln(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertErr(ref);
    }

    @Test
    public void testApply_printf_syserr() throws Throwable {

        //prepare
        final String ref = "f:testF:TEST";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.err;
                doPrintf(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertErr(ref);
    }

    @Test
    public void testApply_format_syserr() throws Throwable {

        //prepare
        final String ref = "f:testF:TEST";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.err;
                doFormat(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertErr(ref);
    }

    @Test
    public void testApply_write_syserr() throws Throwable {

        //prepare
        final String ref = "456789";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.err;
                doWrite(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();

        //assert
        assertErr(ref);
    }

    @Test
    public void testApply_append_syserr() throws Throwable {

        //prepare
        final String ref = "cCSQMIDDLE";
        final Statement stmt = new Statement(){

            @Override
            public void evaluate() throws Throwable {
                PrintStream ps = System.err;
                doAppend(ps);
            }
        };

        //act
        subject.apply(stmt, description).evaluate();
        //assert
        assertErr(ref);
    }

    private void doPrint(final PrintStream ps) {

        ps.print(true);
        ps.print('c');
        ps.print(1);
        ps.print(1.2f);
        ps.print(new char[]{'a','b','c'});
        ps.print("TEST");
        ps.print(new TestObject());
    }

    private void doPrintln(final PrintStream ps) {

        ps.println();
        ps.println(true);
        ps.println('c');
        ps.println(1);
        ps.println(1.2f);
        ps.println(new char[]{'a','b','c'});
        ps.println("TEST");
        ps.println(new TestObject());
    }

    private void doPrintf(final PrintStream ps) {

        ps.printf("f:%s","test");
        ps.printf(Locale.ENGLISH,"F:%s","TEST");
    }

    private void doFormat(final PrintStream ps) {

        ps.format("f:%s", "test");
        ps.format(Locale.ENGLISH, "F:%s", "TEST");
    }

    private void doWrite(final PrintStream ps) throws IOException {

        ps.write(52);
        ps.write(new byte[]{53,54,55});
        ps.write(new byte[]{55,56,57,58}, 1, 2);
    }

    private void doAppend(final PrintStream ps) {

        ps.append('c');
        ps.append("CSQ");
        ps.append("_MIDDLE_",1,7);
    }

    public class TestObject {

        @Override
        public String toString() {
            return "OBJECT";
        }
    }
}

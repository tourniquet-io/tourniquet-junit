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

import static org.junit.Assert.assertNotNull;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * This rule may be used to record output written onto the System.out or System.err print streams. The output will
 * still be written to those stream, but the content written is available as String.
 *
 * Created by Gerald Muecke on 19.11.2015.
 */
public class SystemConsole implements TestRule {

    private RecordingPrintStream err;
    private RecordingPrintStream out;

    @Override
    public Statement apply(final Statement statement, final Description description) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                final PrintStream originalOut = System.out; //NOSONAR
                final PrintStream originalErr = System.err; //NOSONAR
                try {
                    out = new RecordingPrintStream(originalOut);
                    err = new RecordingPrintStream(originalErr);
                    System.setOut(out);
                    System.setErr(err);
                    statement.evaluate();
                } finally {
                    System.setOut(originalOut);
                    System.setErr(originalErr);
                }
            }
        };
    }

    public String getOut(){
        assertNotNull("Rule is not initialized", out);
        return out.getString();
    }

    public String getErr(){
        assertNotNull("Rule is not initialized", err);
        return err.getString();
    }

    /**
     * Proxy that records all written bytes into a StringBuilder that can
     */
    private static class RecordingPrintStream extends PrintStream {

        private final StringBuilder buf = new StringBuilder(64);

        public RecordingPrintStream(final OutputStream out) {
            super(out);
        }

        @Override
        public void write(final int aByte) {
            buf.append((char)aByte);
            super.write(aByte);
        }

        @Override
        public void write(final byte[] buf, final int off, final int len) {
            for(int i = off; i < off + len; i++) {
                write(buf[i]);
            }
            super.write(buf, off, len);
        }

        String getString(){
            return buf.toString();
        }
    }
}

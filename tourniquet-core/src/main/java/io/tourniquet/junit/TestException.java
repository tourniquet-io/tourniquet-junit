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

/**
 * The test exception is an unchecked runtime exception that can be thrown during test execution to indicate an
 * error in the test execution flow.
 */
public class TestException extends RuntimeException {

    private static final long serialVersionUID = -8179466218697657656L;

    public TestException() {
        super("TestException");
    }

    public TestException(final String message) {

        super(message);
    }

    public TestException(final String message, final Throwable cause) {

        super(message, cause);
    }

    public TestException(final Throwable cause) {

        super(cause);
    }
}

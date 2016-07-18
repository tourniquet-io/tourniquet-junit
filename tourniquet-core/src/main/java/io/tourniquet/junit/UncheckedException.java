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
 * Use this exception to indicate that a checked exception was made explicitly unchecked, i.e. to be used with lambdas
 * etc.
 */
public class UncheckedException extends RuntimeException {

    private static final long serialVersionUID = -8179466218697657656L;

    public UncheckedException(final String message, final Throwable cause) {

        super(message, cause);
    }

    public UncheckedException(final Throwable cause) {

        super(cause);
    }
}

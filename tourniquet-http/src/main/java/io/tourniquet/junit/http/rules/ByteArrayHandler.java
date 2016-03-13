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

package io.tourniquet.junit.http.rules;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Handler for serving data from a byte array.
 * Created by Gerald Muecke on 11.12.2015.
 */
public class ByteArrayHandler extends ResourceHttpHandler {

    private final byte[] data;

    /**
     * Creates a new byte array handler.
     * @param resource
     *  the data to be served by this handler. The array is copied so that modifications to it won't affect the data
     *  served by this handler.
     */
    public ByteArrayHandler(final byte[] resource) { //NOSONAR
        this.data = new byte[resource.length];
        System.arraycopy(resource, 0, this.data, 0, resource.length);
    }

    @Override
    protected void writeResource(final OutputStream outputStream) throws IOException {
        outputStream.write(data, 0, data.length);
    }
}

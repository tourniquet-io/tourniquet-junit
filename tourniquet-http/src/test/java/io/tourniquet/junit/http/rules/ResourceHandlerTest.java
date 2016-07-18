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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ResourceHandlerTest {


    @Rule
    public HttpServerExchangeMock exchange = new HttpServerExchangeMock();

    /**
     * The class under test
     */
    private ResourceHandler subject;

    @Before
    public void setUp() throws Exception {
        subject = new ResourceHandler() {

            @Override
            protected void writeResource(final OutputStream outputStream, String queryString) throws IOException {
                final byte[] data = "test".getBytes();
                outputStream.write(data, 0, data.length);
            }
        };
    }

    @Test
    public void testHandleRequest() throws Exception {
        //prepare
        exchange.getExchange().startBlocking();
        //act
        subject.accept(new HttpExchange(exchange.getExchange()));

        //assert
        exchange.getBuffer().rewind();
        byte[] data = new byte[4];
        exchange.getBuffer().get(data);
        assertEquals("test", new String(data));
    }
}

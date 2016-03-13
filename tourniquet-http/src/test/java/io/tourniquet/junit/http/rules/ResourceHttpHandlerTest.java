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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import io.undertow.connector.ByteBufferPool;
import io.undertow.connector.PooledByteBuffer;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Created by Gerald Muecke on 14.12.2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceHttpHandlerTest {


    @Mock
    private ServerConnection serverConnection;

    @Mock
    private ByteBufferPool byteBufferPool;

    @Mock
    private PooledByteBuffer pooledByteBuffer;

    /**
     * The class under test
     */
    private ResourceHttpHandler subject;

    @Before
    public void setUp() throws Exception {
        subject = new ResourceHttpHandler() {

            @Override
            protected void writeResource(final OutputStream outputStream) throws IOException {
                final byte[] data = "test".getBytes();
                outputStream.write(data, 0, data.length);
            }
        };
    }

    @Test
    public void testHandleRequest() throws Exception {
        //prepare
        final ByteBuffer buffer = ByteBuffer.allocate(8);
        final HttpServerExchange exchange = new HttpServerExchange(serverConnection);
        when(serverConnection.getByteBufferPool()).thenReturn(byteBufferPool);
        when(byteBufferPool.allocate()).thenReturn(pooledByteBuffer);
        when(pooledByteBuffer.getBuffer()).thenReturn(buffer);

        //act
        subject.handleRequest(exchange);

        //assert
        buffer.rewind();
        byte[] data = new byte[4];
        buffer.get(data);
        assertEquals("test", new String(data));
    }
}

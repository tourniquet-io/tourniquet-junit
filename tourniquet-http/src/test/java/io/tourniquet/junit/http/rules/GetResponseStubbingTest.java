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

import static org.mockito.Mockito.verify;

import java.nio.charset.Charset;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GetResponseStubbingTest {

    @Mock
    private HttpServer httpServer;

    /**
     * The class under test
     */
    @InjectMocks
    private GetResponseStubbing subject;


    @Test
    public void testRespond() throws Exception {
        //prepare
        subject.resource("path");

        //act
        subject.respond("test");


        //assert
        verify(httpServer).addResource("path", Optional.empty(), "test".getBytes(Charset.defaultCharset()));
    }

    @Test
    public void testRespond_toQuery() throws Exception {
        //prepare
        subject.resource("path?param=value");

        //act
        subject.respond("test");


        //assert
        verify(httpServer).addResource("path", Optional.of("param=value"), "test".getBytes(Charset.defaultCharset()));
    }

}

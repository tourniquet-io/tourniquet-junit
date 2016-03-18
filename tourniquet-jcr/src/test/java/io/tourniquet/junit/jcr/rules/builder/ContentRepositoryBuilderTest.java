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

package io.tourniquet.junit.jcr.rules.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.net.URL;
import org.junit.Test;
import org.junit.rules.TestRule;

public class ContentRepositoryBuilderTest {

    private ContentRepositoryBuilder subject = new ContentRepositoryBuilder() {

        @Override
        public TestRule build() {

            return null;
        }
    };

    @Test
    public void testAroundSession() throws Exception {

        //prepare

        //act
        JCRSessionBuilder result = subject.aroundSession();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testAroundPreparedContent() throws Exception {

        //act
        ContentLoaderBuilder result = subject.aroundPreparedContent();

        //assert
        assertNotNull(result);
    }

    @Test
    public void testWithNodeTypesFromCnd() throws Exception {

        //prepare
        URL resource = new URL("http://localhost");

        //act
        ContentRepositoryBuilder result = subject.withNodeTypes(resource);
        URL actualResource = subject.getCndModelResource();

        //assert
        assertNotNull(result);
        assertSame(result, subject);
        assertEquals(resource, actualResource);
    }
}

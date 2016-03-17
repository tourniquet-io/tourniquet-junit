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

package io.tourniquet.junit.util;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 */
public class ClassStreamsTest {

    @Test
    public void testSupertypes() throws Exception {

        //arrange

        //act
        List<Class> superTypes = ClassStreams.supertypes(Leaf.class).collect(Collectors.toList());

        //assert
        assertThat(superTypes, hasSize(2));
        assertThat(superTypes, contains(Intermediate.class, BaseType.class));
    }

    @Test
    public void testSelfAndSupertypes() throws Exception {

        //arrange

        //act
        List<Class> superTypes = ClassStreams.selfAndSupertypes(Leaf.class).collect(Collectors.toList());

        //assert
        assertThat(superTypes, hasSize(3));
        assertThat(superTypes, contains(Leaf.class, Intermediate.class, BaseType.class));
    }

    static class BaseType {

    }

    static class Intermediate extends BaseType {

    }

    static class Leaf extends Intermediate {

    }
}

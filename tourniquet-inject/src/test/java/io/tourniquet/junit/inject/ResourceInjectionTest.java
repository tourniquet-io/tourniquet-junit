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

package io.tourniquet.junit.inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Resource;
import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ResourceInjectionTest {

    private String injectionValue;
    private ResourceInjection subject;

    @Before
    public void setUp() throws Exception {
        injectionValue = "testValue";
        subject = new ResourceInjection(injectionValue);
    }

    @Test
    public void testIsMatching_noResource_false() throws Exception {
        final Field field = SimpleInjectionTarget.class.getDeclaredField("noResource");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_byMatchingTypeOnly_false() throws Exception {
        final Field field = SimpleInjectionTarget.class.getDeclaredField("simpleResource");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_byNoMatchingType_false() throws Exception {
        final Field field = SimpleInjectionTarget.class.getDeclaredField("noMatchingType");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testByName_match() throws Exception {
        // prepare
        final Field field = SimpleInjectionTarget.class.getDeclaredField("namedResource");
        // act
        subject.byName("target2");
        // assert
        assertTrue(subject.isMatching(field));
    }

    @Test
    public void testByName_noMatch() throws Exception {
        // prepare
        final Field field = SimpleInjectionTarget.class.getDeclaredField("namedResource");
        // act
        subject.byName("noMatch");
        // assert
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testByMappedName_match() throws Exception {
        // prepare
        final Field field = SimpleInjectionTarget.class.getDeclaredField("mappedNameResource");
        // act
        subject.byMappedName("target3");
        // assert
        assertTrue(subject.isMatching(field));
    }

    @Test
    public void testByMappedName_noMatch() throws Exception {
        // prepare
        final Field field = SimpleInjectionTarget.class.getDeclaredField("mappedNameResource");
        // act
        subject.byMappedName("noMatch");
        // assert
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testByLookup_match() throws Exception {
        // prepare
        final Field field = SimpleInjectionTarget.class.getDeclaredField("lookupResource");
        // act
        subject.byLookup("target1");
        // assert
        assertTrue(subject.isMatching(field));
    }

    @Test
    public void testByLookup_noMatch() throws Exception {
        // prepare
        final Field field = SimpleInjectionTarget.class.getDeclaredField("lookupResource");
        // act
        subject.byLookup("noMatch");
        // assert
        assertFalse(subject.isMatching(field));
    }

    static class SimpleInjectionTarget {

        @Resource(lookup = "target1")
        String lookupResource;

        @Resource(name = "target2")
        String namedResource;

        @Resource(mappedName = "target3")
        String mappedNameResource;

        @Resource
        String simpleResource;

        @Resource
        Integer noMatchingType;

        String noResource;
    }

}

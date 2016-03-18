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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;

import org.junit.Test;

public class CdiInjectionTest {

    @Test
    public void testGetValue() throws Exception {
        //prepare

        String value = "123";

        //act
        CdiInjection subject = new CdiInjection(value);

        //assert
        assertEquals(value, subject.getValue());

    }

    @Test
    public void testIsMatching_matchingTypeAndInjectPresent() throws Exception {
        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value);

        //act

        Field field = InjectField.class.getDeclaredField("field");
        boolean result = subject.isMatching(field);

        //assert
        assertTrue(result);
    }

    @Test
    public void testIsMatching_matchingTypeAndNoQualifiedInjectPresent() throws Exception {


        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value);

        //act
        Field field = QualifiedInjectField.class.getDeclaredField("field");
        Field qualifiedField = QualifiedInjectField.class.getDeclaredField("qualifiedField");
        Field additionalField = QualifiedInjectField.class.getDeclaredField("additionalField");
        //assert
        assertTrue(subject.isMatching(field));
        assertTrue(subject.isMatching(qualifiedField));
        assertTrue(subject.isMatching(additionalField));
    }

    @Test
    public void testIsMatching_withDefaultQualifier() throws Exception {


        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value, Default.class);

        //act
        Field nField = DefaultQualifiedInjectField.class.getDeclaredField("nField");
        Field qField = DefaultQualifiedInjectField.class.getDeclaredField("qField");
        Field aField = DefaultQualifiedInjectField.class.getDeclaredField("aField");
        //assert
        assertTrue(subject.isMatching(nField));
        assertTrue(subject.isMatching(qField));
        assertFalse(subject.isMatching(aField));
    }

    @Test
    public void testIsMatching_withoutDefaultQualifier() throws Exception {


        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value);

        //act
        Field nField = DefaultQualifiedInjectField.class.getDeclaredField("nField");
        Field qField = DefaultQualifiedInjectField.class.getDeclaredField("qField");
        Field aField = DefaultQualifiedInjectField.class.getDeclaredField("aField");
        //assert
        assertTrue(subject.isMatching(nField));
        assertTrue(subject.isMatching(qField));
        assertTrue(subject.isMatching(aField));
    }

    @Test
    public void testIsMatching_matchingTypeAndOneQualifiedInjectPresent() throws Exception {
        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value, AQualifier.class);

        //act
        Field field = QualifiedInjectField.class.getDeclaredField("field");
        Field qualifiedField = QualifiedInjectField.class.getDeclaredField("qualifiedField");
        Field additionalField = QualifiedInjectField.class.getDeclaredField("additionalField");
        //assert
        assertFalse(subject.isMatching(field));
        assertTrue(subject.isMatching(qualifiedField));
        assertTrue(subject.isMatching(additionalField));
    }

    @Test
    public void testIsMatching_matchingTypeAndManyQualifiedInjectPresent() throws Exception {
        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value, AQualifier.class, BQualifier.class);

        //act
        Field field = QualifiedInjectField.class.getDeclaredField("field");
        Field qualifiedField = QualifiedInjectField.class.getDeclaredField("qualifiedField");
        Field additionalField = QualifiedInjectField.class.getDeclaredField("additionalField");
        //assert
        assertFalse(subject.isMatching(field));
        assertFalse(subject.isMatching(qualifiedField));
        assertTrue(subject.isMatching(additionalField));
    }

    @Test
    public void testIsMatching_matchingTypeAndNoInjectPresent() throws Exception {
        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value);

        //act

        Field field = NoInjectField.class.getDeclaredField("field");
        boolean result = subject.isMatching(field);

        //assert
        assertFalse(result);

    }

    @Test
    public void testIsMatching_noMatchingTypeAndInjectPresent() throws Exception {


        //prepare
        String value = "123";
        CdiInjection subject = new CdiInjection(value);

        //act

        Field field = NoMatchingField.class.getDeclaredField("field");
        boolean result = subject.isMatching(field);

        //assert
        assertFalse(result);

    }

    static class NoInjectField {

        private String field;
    }

    static class InjectField {

        @Inject
        private String field;
    }

    static class NoMatchingField {

        @Inject
        private Integer field;
    }

    static class DefaultQualifiedInjectField {

        @Inject
        private String nField;

        @Default
        @Inject
        private String qField;

        @AQualifier
        @Inject
        private String aField;
    }

    static class QualifiedInjectField {

        @Inject
        private String field;

        @AQualifier
        @Inject
        private String qualifiedField;

        @AQualifier
        @BQualifier
        @Inject
        private String additionalField;
    }

    @Qualifier
    @Retention(RUNTIME)
    @interface AQualifier {}

    @Qualifier
    @Retention(RUNTIME)
    @interface BQualifier {}

}

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
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Mockito.when;

import javax.inject.Qualifier;
import java.lang.annotation.Retention;
import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class InjectionTest {

    private SimpleInjectionTarget injectionTarget;
    private String injectionValue;
    private Injection subject;

    @Mock
    private InjectableHolder injectableHolder;

    @Before
    public void setUp() throws Exception {
        injectionTarget = new SimpleInjectionTarget();
        injectionValue = "testValue";
        subject = new Injection(injectionValue);
    }

    @Test
    public void testInjection_InjectableHolder() throws Exception {
        //prepare
        String value = "123";
        when(injectableHolder.getInjectionValue()).thenReturn(value);

        //act
        Injection injection = new Injection(injectableHolder);

        //assert
        assertEquals(value, injection.getValue());
    }

    @Test
    public void testInjection_AnyObject() throws Exception {
        //prepare
        Integer value = 123;

        //act
        Injection injection = new Injection(value);

        //assert
        assertEquals(value, injection.getValue());
    }

    @Test
    public void testAsResource() throws Exception {
        assertNotNull(subject.asResource());
        // what happens with a resource subject is defined in the ResourceInjectionTest
    }

    @Test
    public void testAsQualifyingInstace_noQualifier() throws Exception {

        assertNotNull(subject.asQualifyingInstance());
        //see CdiInjectionTest for more details

    }

    @Test
    public void testAsQualifyingInstance_withQualifier() throws Exception {

        assertNotNull(subject.asQualifyingInstance(TestQualifier.class));
        //see CdiInjectionTest for more details

    }

    @Test
    public void testAsConfigProperty() throws Exception {
        final ConfigPropertyInjection cpi = subject.asConfigProperty("myProperty");
        assertNotNull(cpi);
        // what happens with a configProperty subject is defined in the ConfigPropertyInjectionTest
    }

    @Test
    public void testIntoAll() throws Exception {
        // act
        subject.intoAll(injectionTarget);
        // assert
        assertEquals(injectionValue, injectionTarget.injectionTarget1);
        assertEquals(injectionValue, injectionTarget.injectionTarget2);
        assertNull(injectionTarget.injectionTarget3);
    }

    @Test
    public void testInto() throws Exception {
        // the test assumes the of the fields is always the same as defined in the class
        assumeTrue("injectionTarget1".equals(SimpleInjectionTarget.class.getDeclaredFields()[0].getName()));
        // only the first match should be injected
        // act
        subject.into(injectionTarget);
        // assert
        assertEquals(injectionValue, injectionTarget.injectionTarget1);
        assertNull(injectionTarget.injectionTarget2);
        assertNull(injectionTarget.injectionTarget3);
    }

    @Test(expected = AssertionError.class)
    public void testInto_nullTarget() throws Exception {
        //prepare
        final Injection subject = new Injection(null);
        // act
        subject.into(null);
    }

    @Test
    public void testInto_primitiveType_byte() throws Exception {

        byte var = (byte) 123;
        test_into_primitiveField("primitiveByte", var);
    }

    private void test_into_primitiveField(String fieldname, Object primitiveValue)
            throws NoSuchFieldException, IllegalAccessException {

        //prepare
        final Injection subject = new Injection(primitiveValue);
        final SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        subject.into(target);

        //assert
        final Field field = SimpleInjectionTarget.class.getDeclaredField(fieldname);
        assertEquals(primitiveValue, field.get(target));
    }

    @Test
    public void testInto_primitiveType_short() throws Exception {

        short var = (short) 123;
        test_into_primitiveField("primitiveShort", var);
    }

    @Test
    public void testInto_primitiveType_int() throws Exception {

        int var = 123;
        test_into_primitiveField("primitiveInt", var);
    }

    @Test
    public void testInto_primitiveType_long() throws Exception {

        long var = (long) 123;
        test_into_primitiveField("primitiveLong", var);
    }

    @Test
    public void testInto_primitiveType_float() throws Exception {

        float var = (float) 123;
        test_into_primitiveField("primitiveFloat", var);
    }

    @Test
    public void testInto_primitiveType_double() throws Exception {

        double var = (double) 123;
        test_into_primitiveField("primitiveDouble", var);
    }

    @Test
    public void testInto_primitiveType_char() throws Exception {

        char var = (char) 123;
        test_into_primitiveField("primitiveChar", var);
    }

    @Test
    public void testInto_primitiveType_boolean() throws Exception {

        boolean var = true;
        test_into_primitiveField("primitiveBoolean", var);
    }

    @Test(expected = AssertionError.class)
    public void testInto_noMatchingTarget_fail() throws Exception {
        //prepare
        final Injection subject = new Injection(new NonMatchingType());
        final SimpleInjectionTarget target = new SimpleInjectionTarget();

        //act
        // SCRIB-29 this should fail as there is no matching field
        subject.into(target);

    }

    @Test
    public void testGetValue() throws Exception {
        assertEquals(injectionValue, subject.getValue());
    }

    @Test
    public void testGetDefaultValue() throws Exception {
        assertNull(subject.getDefaultValue(null));
    }

    @Test
    public void testIsMatching_nullValue_True() throws Exception {
        final Injection subject = new Injection(null);
        final Field field = SimpleInjectionTarget.class.getDeclaredField("injectionTarget3");
        assertTrue(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_compatibleField_true() throws Exception {
        final Field field = SimpleInjectionTarget.class.getDeclaredField("injectionTarget1");
        assertTrue(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_primitiveBooleanField_true() throws Exception {

        test_isMatching_primitiveField("primitiveBoolean", true);
    }

    private void test_isMatching_primitiveField(String fieldname, Object primitiveValue) throws NoSuchFieldException {

        final Field field = SimpleInjectionTarget.class.getDeclaredField(fieldname);
        assertTrue(new Injection(primitiveValue).isMatching(field));
    }

    @Test
    public void testIsMatching_primitiveByteField_true() throws Exception {

        test_isMatching_primitiveField("primitiveByte", (byte) 79);
    }

    @Test
    public void testIsMatching_primitiveShortField_true() throws Exception {

        test_isMatching_primitiveField("primitiveShort", (short) 123);
    }

    @Test
    public void testIsMatching_primitiveIntField_true() throws Exception {

        test_isMatching_primitiveField("primitiveInt", 123);
    }

    @Test
    public void testIsMatching_primitiveLongField_true() throws Exception {

        test_isMatching_primitiveField("primitiveLong", (long) 123);
    }

    @Test
    public void testIsMatching_primitiveFloatField_true() throws Exception {

        test_isMatching_primitiveField("primitiveFloat", (float) 123);
    }

    @Test
    public void testIsMatching_primitiveDoubleField_true() throws Exception {

        test_isMatching_primitiveField("primitiveDouble", (double) 123);
    }

    @Test
    public void testIsMatching_primitiveCharField_true() throws Exception {

        test_isMatching_primitiveField("primitiveChar", (char) 123);
    }

    @Test
    public void testIsMatching_incompatibleField_false() throws Exception {
        final Field field = SimpleInjectionTarget.class.getDeclaredField("injectionTarget3");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testInject() throws Exception {
        //prepare
        String value = "123";
        SimpleInjectionTarget target = new SimpleInjectionTarget();
        Field field = SimpleInjectionTarget.class.getDeclaredField("injectionTarget1");
        field.setAccessible(true);

        //act
        subject.inject(target, field, value);

        //assert
        assertEquals(value, target.injectionTarget1);

    }

    static class NonMatchingType {

    }

    static class AnyType {

    }

    static class SimpleInjectionTarget {

        String injectionTarget1;
        String injectionTarget2;
        AnyType injectionTarget3;

        boolean primitiveBoolean;
        byte primitiveByte;
        short primitiveShort;
        int primitiveInt;
        long primitiveLong;
        float primitiveFloat;
        double primitiveDouble;
        char primitiveChar;
    }

    @Qualifier
    @Retention(RUNTIME)
    @interface TestQualifier{

    }

}

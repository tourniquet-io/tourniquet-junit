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

import static org.junit.Assert.*;

import javax.inject.Inject;
import java.lang.reflect.Field;

import org.apache.deltaspike.core.api.config.ConfigProperty;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConfigPropertyInjectionTest {

    private ConfigPropertyInjection subject;

    @Before
    public void setUp() throws Exception {

        subject = new ConfigPropertyInjection("config.property", "testValue");
    }

    @After
    public void tearDown() throws Exception {

    }



    @Test
    public void testInjectInto_NullValue_AutoConverted_primitiveTypes_defaultInjected() throws Exception {
        //prepare
        String var = null;
        test_autoConversionDefaultValue_into("booleanProperty");
        test_autoConversionDefaultValue_into("charProperty");
        test_autoConversionDefaultValue_into("byteProperty");
        test_autoConversionDefaultValue_into("shortProperty");
        test_autoConversionDefaultValue_into("intProperty");
        test_autoConversionDefaultValue_into("longProperty");
        test_autoConversionDefaultValue_into("floatProperty");
        test_autoConversionDefaultValue_into("doubleProperty");
    }

    private void test_autoConversionDefaultValue_into(String fieldName)
            throws NoSuchFieldException, IllegalAccessException {

        //prepare
        Object injectedValue = null;
        final ConfigPropertyInjection injection = new ConfigPropertyInjection(fieldName, injectedValue);
        final AutoConversionInjectionTarget target = new AutoConversionInjectionTarget();

        //act
        injection.into(target);

        //assert
        final Field field = AutoConversionInjectionTarget.class.getDeclaredField(fieldName);
        String defaultValue = field.getAnnotation(ConfigProperty.class).defaultValue();

        assertEquals(TypeUtil.convert(defaultValue, field.getType()), field.get(target));
    }

    @Test
    public void testInjectInto_NullValue_AutoConverted_objectTypes_defaultInjected() throws Exception {
        //prepare
        String var = null;
        test_autoConversionDefaultValue_into("BooleanProperty");
        test_autoConversionDefaultValue_into("CharacterProperty");
        test_autoConversionDefaultValue_into("ByteProperty");
        test_autoConversionDefaultValue_into("ShortProperty");
        test_autoConversionDefaultValue_into("IntegerProperty");
        test_autoConversionDefaultValue_into("LongProperty");
        test_autoConversionDefaultValue_into("FloatProperty");
        test_autoConversionDefaultValue_into("DoubleProperty");
    }

    @Test
    public void testInjectInto_StringValue_AutoConverted_primitiveTypes_valueInjected() throws Exception {
        //prepare
        String var = "123";

        //act
        test_autoConversion_into("booleanProperty", "true");
        test_autoConversion_into("charProperty", "c");
        test_autoConversion_into("byteProperty", var);
        test_autoConversion_into("shortProperty", var);
        test_autoConversion_into("intProperty", var);
        test_autoConversion_into("longProperty", var);
        test_autoConversion_into("floatProperty", var);
        test_autoConversion_into("doubleProperty", var);
    }

    private void test_autoConversion_into(String fieldName, String value)
            throws NoSuchFieldException, IllegalAccessException {

        //prepare
        final ConfigPropertyInjection injection = new ConfigPropertyInjection(fieldName, value);
        final AutoConversionInjectionTarget target = new AutoConversionInjectionTarget();

        //act
        injection.into(target);

        //assert
        final Field field = AutoConversionInjectionTarget.class.getDeclaredField(fieldName);
        assertEquals(TypeUtil.convert(value, field.getType()), field.get(target));
    }

    @Test
    public void testInjectInto_StringValue_AutoConverted_objectTypes_valueInjected() throws Exception {
        //prepare
        String var = "123";

        //act
        test_autoConversion_into("BooleanProperty", "true");
        test_autoConversion_into("CharacterProperty", "c");
        test_autoConversion_into("ByteProperty", var);
        test_autoConversion_into("ShortProperty", var);
        test_autoConversion_into("IntegerProperty", var);
        test_autoConversion_into("LongProperty", var);
        test_autoConversion_into("FloatProperty", var);
        test_autoConversion_into("DoubleProperty", var);
    }



    @Test
    public void testGetValue() throws Exception {

        assertEquals("testValue", subject.getValue());
    }

    @Test
    public void testGetDefaultValue_defaultValueOfMatchingConfigProperty() throws Exception {
        // prepare
        // create an injection with no value (null) for a config property with a default value
        final ConfigPropertyInjection subject = new ConfigPropertyInjection("config.property.default", null);
        assertNull(subject.getValue());
        // get the field of the config property that has a default value and matches the injection name
        final Field field = SimpleInjectionTarget.class.getDeclaredField("configPropertyWithDefault");

        // act
        final String value = (String) subject.getDefaultValue(field);

        // assert
        assertNotNull(value);
        assertEquals("defaultValue", value);
    }

    @Test
    public void testIsMatching_configPropertyNameMatch_True() throws Exception {

        final Field field = SimpleInjectionTarget.class.getDeclaredField("configProperty");
        assertTrue(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_configPropertyNameMismatch_false() throws Exception {

        final Field field = SimpleInjectionTarget.class.getDeclaredField("configPropertyWithDefault");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_configPropertyUndefined_false() throws Exception {

        ConfigPropertyInjection subject = new ConfigPropertyInjection("noDefinedProperty", null);
        final Field field = SimpleInjectionTarget.class.getDeclaredField("configProperty");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_noInjectConfigProperty_false() throws Exception {

        final Field field = SimpleInjectionTarget.class.getDeclaredField("noInjectConfigProperty");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testIsMatching_noConfigPropertyAnnotation_false() throws Exception {

        final Field field = SimpleInjectionTarget.class.getDeclaredField("noConfigProperty");
        assertFalse(subject.isMatching(field));
    }

    @Test
    public void testIsFieldCandidate_noConfigPropertyAnnotation_false() throws Exception {
        //prepare
        Field field = SimpleInjectionTarget.class.getDeclaredField("noConfigProperty");

        //act
        boolean result = subject.isFieldCandidate(field, "");

        //assert
        assertFalse(result);
    }

    @Test
    public void testIsFieldCandidate_injectedValueNull_true() throws Exception {
        //prepare
        Field field = SimpleInjectionTarget.class.getDeclaredField("configProperty");

        //act
        boolean result = subject.isFieldCandidate(field, null);

        //assert
        assertTrue(result);

    }

    @Test
    public void testIsFieldCandidate_matchingField_true() throws Exception {
        //prepare
        Field field = SimpleInjectionTarget.class.getDeclaredField("configProperty");

        //act
        boolean result = subject.isFieldCandidate(field, "");

        //assert
        assertTrue(result);
    }

    @Test
    public void testIsFieldCandidate_notMatchingField_false() throws Exception {
        //prepare
        Field field = SimpleInjectionTarget.class.getDeclaredField("configProperty");

        //act
        boolean result = subject.isFieldCandidate(field, new Object());

        //assert
        assertFalse(result);
    }

    @Test
    public void testIsFieldCandidate_autoConvertible_true() throws Exception {
        //prepare
        Field field = SimpleInjectionTarget.class.getDeclaredField("autoConvertibleField");

        //act
        boolean result = subject.isFieldCandidate(field, "123");

        //assert
        assertTrue(result);
    }

    static class SimpleInjectionTarget {

        @ConfigProperty(name = "config.property")
        String noInjectConfigProperty;

        @Inject
        @ConfigProperty(name = "config.property")
        String configProperty;

        @Inject
        @ConfigProperty(name = "config.property.default",
                        defaultValue = "defaultValue")
        String configPropertyWithDefault;

        @Inject
        String noConfigProperty;

        @Inject
        @ConfigProperty(name = "config.property")
        int autoConvertibleField;

        @Inject
        @ConfigProperty(name = "config.property")
        Object nonMatchingNonAutoConvertible;
    }

    static class AutoConversionInjectionTarget {

        @Inject
        @ConfigProperty(name = "booleanProperty",
                        defaultValue = "true")
        boolean booleanProperty;

        @Inject
        @ConfigProperty(name = "charProperty",
                        defaultValue = "a")
        boolean charProperty;

        @Inject
        @ConfigProperty(name = "byteProperty",
                        defaultValue = "123")
        byte byteProperty;

        @Inject
        @ConfigProperty(name = "shortProperty",
                        defaultValue = "123")
        short shortProperty;

        @Inject
        @ConfigProperty(name = "intProperty",
                        defaultValue = "123")
        int intProperty;

        @Inject
        @ConfigProperty(name = "longProperty",
                        defaultValue = "123")
        long longProperty;

        @Inject
        @ConfigProperty(name = "floatProperty",
                        defaultValue = "123")
        float floatProperty;

        @Inject
        @ConfigProperty(name = "doubleProperty",
                        defaultValue = "123")
        double doubleProperty;

        @Inject
        @ConfigProperty(name = "BooleanProperty",
                        defaultValue = "true")
        Boolean BooleanProperty;

        @Inject
        @ConfigProperty(name = "CharacterProperty",
                        defaultValue = "a")
        Character CharacterProperty;

        @Inject
        @ConfigProperty(name = "ByteProperty",
                        defaultValue = "123")
        Byte ByteProperty;

        @Inject
        @ConfigProperty(name = "ShortProperty",
                        defaultValue = "123")
        Short ShortProperty;

        @Inject
        @ConfigProperty(name = "IntegerProperty",
                        defaultValue = "123")
        Integer IntegerProperty;

        @Inject
        @ConfigProperty(name = "LongProperty",
                        defaultValue = "123")
        Long LongProperty;

        @Inject
        @ConfigProperty(name = "FloatProperty",
                        defaultValue = "123")
        Float FloatProperty;

        @Inject
        @ConfigProperty(name = "DoubleProperty",
                        defaultValue = "123")
        Double DoubleProperty;
    }

}

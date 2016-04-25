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

package io.tourniquet.pageobjects;

import static io.tourniquet.pageobjects.Locator.ByLocator.ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import io.tourniquet.selenium.SeleniumContext;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PageObjectsInjectorTest {

    @Mock
    private Description description;

    @Mock
    private WebElement element;

    @Mock
    private WebDriver driver;

    @After
    public void tearDown() throws Exception {
        SeleniumContext.currentContext().ifPresent(SeleniumContext::destroy);
    }

    @Test
    public void testInjectMethods() throws Exception {
        //prepare
        ChildMethodInjectTestGroup group = new ChildMethodInjectTestGroup();

        //act
        PageObjectsInjector.injectMethods(group);

        //assert
        //direct injection
        assertNotNull(group.child);
        //superclass injection
        assertNotNull(group.field);
    }

    @Test
    public void testInjectFields() throws Throwable {

        //prepare
        ChildFieldInjectTestGroup group = new ChildFieldInjectTestGroup();
        new SeleniumContext(() -> driver).init();
        when(driver.findElement(By.id("subgroup"))).thenReturn(element);
        //act
        PageObjectsInjector.injectFields(group);


        //assert
        //direct injection
        assertNotNull(group.child);
        //superclass injection
        assertNotNull(group.field);
        //element subgroup injection
        assertNotNull(group.group);
        assertNotNull(group.group.field);
        assertNotNull(group.ctxGroup.field);
        assertNotNull(group.ctxGroup.field);

    }

    @Test
    public void testAbstractGroup_Click() throws Exception {
        //prepare
        new SeleniumContext(() -> driver).init();
        WebElement element = mock(WebElement.class);
        when(driver.findElement(By.id("someButton"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        GroupWithAbstractSubgroup group = new GroupWithAbstractSubgroup();

        //act
        PageObjectsInjector.injectFields(group);

        //assert
        assertNotNull(group.subgroup);
        WebElement e = group.subgroup.pressButton();
        verify(element).click();
        assertNotNull(e);
        assertEquals(element, e);
        assertEquals("testpage", group.subgroup.toString());
    }

    @Test
    public void testAbstractGroup_SubmitForm() throws Exception {
        //prepare
        new SeleniumContext(() -> driver).init();
        WebElement element = mock(WebElement.class);
        when(driver.findElement(By.id("someForm"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        when(element.getTagName()).thenReturn("form");
        GroupWithAbstractSubgroup group = new GroupWithAbstractSubgroup();

        //act
        PageObjectsInjector.injectFields(group);

        //assert
        assertNotNull(group.subgroup);
        group.subgroup.submitForm();
        verify(element).submit();
        assertEquals("testpage", group.subgroup.toString());
    }

    @Test
    public void testAbstractGroup_sendKeys() throws Exception {
        //prepare
        new SeleniumContext(() -> driver).init();
        WebElement element = mock(WebElement.class);
        when(driver.findElement(By.id("someInput"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        GroupWithAbstractSubgroup group = new GroupWithAbstractSubgroup();

        //act
        PageObjectsInjector.injectFields(group);

        //assert
        assertNotNull(group.subgroup);
        AbstractGroup fluentPage = group.subgroup.enterValue("text");
        verify(element).clear();
        verify(element).sendKeys("text");
        assertNotNull(fluentPage);
        assertEquals(group.subgroup, fluentPage);
        assertEquals("testpage", fluentPage.toString());
    }

    @Test(expected = NoSuchMethodException.class)
    public void testAbstractGroup_nonInjectedMethod() throws Exception {
        //prepare
        new SeleniumContext(() -> driver).init();
        GroupWithAbstractSubgroup group = new GroupWithAbstractSubgroup();

        //act
        PageObjectsInjector.injectFields(group);

        //assert
        assertNotNull(group.subgroup);
        //this method will not be implemented
        group.subgroup.unimplemented();

    }

    @Test(expected = IllegalArgumentException.class)
    public void testAbstractGroup_twoManyArgs() throws Exception {
        //prepare
        new SeleniumContext(() -> driver).init();
        WebElement element = mock(WebElement.class);
        when(driver.findElement(By.id("someInput"))).thenReturn(element);
        when(element.isDisplayed()).thenReturn(true);
        GroupWithAbstractSubgroup group = new GroupWithAbstractSubgroup();

        //act
        PageObjectsInjector.injectFields(group);

        //assert
        assertNotNull(group.subgroup);
        //this method is not supported
        group.subgroup.twoParamsMethod("text","2ndParam");
    }

    //// Test Page Object classes

    public static class FieldInjectTestGroup implements ElementGroup {

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;

        SubGroup group;

        @Locator(by = ID, value="subgroup")
        ContextualSubGroup ctxGroup;

    }

    public static class ChildFieldInjectTestGroup extends FieldInjectTestGroup {

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> child;

    }

    public static class SubGroup implements ElementGroup {
        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;
    }

    public static class ContextualSubGroup implements ElementGroup {

        private final SearchContext searchContext;

        @Locator(by = ID, value = "testId")
        Supplier<WebElement> field;

        public ContextualSubGroup(SearchContext ctx){
            this.searchContext = ctx;
        }
    }

    public static class MethodInjectTestGroup implements ElementGroup {

        Supplier<WebElement> field;

        @Locator(by = ID, value = "testId")
        void setField(final Supplier<WebElement> field) {

            this.field = field;
        }
    }

    public static class ChildMethodInjectTestGroup extends MethodInjectTestGroup {

        Supplier<WebElement> child;

        @Locator(by = ID, value = "testId")
        void setChild(final Supplier<WebElement> child) {
            this.child = child;
        }
    }

    public static class GroupWithAbstractSubgroup implements ElementGroup {

        AbstractGroup subgroup;

    }

    public static abstract class AbstractGroup implements ElementGroup {

        @Locator(by = ID, value="someForm")
        public abstract void submitForm();

        @Locator(by = ID, value="someButton")
        public abstract WebElement pressButton();

        @Locator(by = ID, value="someInput")
        public abstract AbstractGroup enterValue(String someText);

        @Locator(by = ID, value="someInput")
        public abstract AbstractGroup twoParamsMethod(String someText, String secondParam);

        public abstract void unimplemented();

        @Override
        public String toString() {

            return "testpage";
        }

    }


}

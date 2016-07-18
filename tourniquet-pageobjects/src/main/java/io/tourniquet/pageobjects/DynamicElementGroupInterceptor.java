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

import static io.tourniquet.selenium.SeleniumContext.currentDriver;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * CGLib method interceptor that injects default implementations for abstract methods that denote an action on an
 * element. The methods must be annotated with {@link Locator} and may have either no or one (String) argument. The
 * return type may be void, the page itself or a web element.
 */
class DynamicElementGroupInterceptor implements MethodInterceptor {

    private final Map<Method, Supplier<WebElement>> elements;

    private Optional<ElementGroup> context = Optional.empty();

    public <T extends ElementGroup> DynamicElementGroupInterceptor(final Class<T> pageType) {
        //scan for abstract methods with locator annotation
        this.elements = Stream.of(pageType.getMethods())
                              .filter(m -> m.getAnnotation(Locator.class) != null)
                              .collect(Collectors.toMap(method -> method,
                                                        method -> (Supplier<WebElement>) () -> WebElementLocator.locate(
                                                                getSearchContext(),
                                                                method.getAnnotation(Locator.class)))

                              );

    }

    @Override
    public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy)
            throws Throwable {

        if (Modifier.isAbstract(method.getModifiers())) {
            if (elements.containsKey(method)) {
                WebElement element = elements.get(method).get();
                if ("form".equals(element.getTagName())) {
                    element.submit();
                } else if (objects.length == 1) {
                    element.clear();
                    element.sendKeys(objects[0].toString());
                } else if (objects.length == 0) {
                    element.click();
                } else {
                    throw new IllegalArgumentException("Method does not match web element " + element);
                }
                if (method.getReturnType().isAssignableFrom(o.getClass())) {
                    return o;
                } else if (method.getReturnType().isAssignableFrom(WebElement.class)) {
                    return element;
                }
                return null;

            } else {
                throw new NoSuchMethodException("Method " + method + " does not provide locator information");
            }

        }
        return methodProxy.invokeSuper(o, objects);
    }

    /**
     * The seach context for locating elements. As search context, the search context of the current element group is
     * used. The default search context is the current driver. So if no driver is initialized in the current
     * SeleniumContext, this method will throw an {@link IllegalStateException}.
     *
     * @return the current search context.
     */
    SearchContext getSearchContext() {

        return context.map(ElementGroup::getSearchContext)
                      .orElse(currentDriver());
    }

    /**
     * Assigns the specified element group to this invocation handler. The search context of the element group is used
     * to lookup for elements that are dynamically located.
     *
     * @param elementGroup
     *         the element group this invocation handle should use to lookup for elements. May be null.
     */
    void assignElementGroup(ElementGroup elementGroup) {

        this.context = Optional.ofNullable(elementGroup);
    }
}

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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.openqa.selenium.WebElement;

/**
 *
 */
class DynamicElementGroupInterceptor implements MethodInterceptor {

    private final Map<Method, Supplier<WebElement>> elements;

    public <T extends Page> DynamicElementGroupInterceptor(final Class<T> pageType) {
        //scan for abstract methods with locator annotation
        this.elements = Stream.of(pageType.getMethods())
                              .filter(m -> m.getAnnotation(Locator.class) != null)
                              .collect(Collectors.toMap(method -> method,
                                                        method -> (Supplier<WebElement>) () -> WebElementLocator.locate(
                                                                method.getAnnotation(Locator.class)))

                              );

    }

    @Override
    public Object intercept(final Object o, final Method method, final Object[] objects, final MethodProxy methodProxy)
            throws Throwable {


        if (Modifier.isAbstract(method.getModifiers())) {
            if(elements.containsKey(method)){
                WebElement element = elements.get(method).get();
                if("form".equals(element.getTagName())){
                    element.submit();
                } else if(objects.length == 1){
                    element.clear();
                    element.sendKeys(objects[0].toString());
                } else if(objects.length == 0) {
                    element.click();
                } else {
                    throw new IllegalArgumentException("Method does not match web element " + element);
                }
                if(o.getClass().isAssignableFrom(method.getReturnType())){
                    return o;
                } else if(WebElement.class.isAssignableFrom(method.getReturnType())){
                    return element;
                }
                return null;

            } else {
                throw new NoSuchMethodException("Method " + method + " does not provide locator information");
            }

        }
        return methodProxy.invokeSuper(o, objects);
    }
}

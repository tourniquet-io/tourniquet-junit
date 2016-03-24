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

import static io.tourniquet.junit.util.ExecutionHelper.runUnchecked;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import io.tourniquet.junit.util.ClassStreams;
import org.openqa.selenium.WebDriver;

/**
 * The selenium context is a container for maintaining access to the current driver of
 */
public class SeleniumContext {

    private static ThreadLocal<Optional<SeleniumContext>> CONTEXT = ThreadLocal.withInitial(() -> Optional.empty());

    private Optional<WebDriver> driver = Optional.empty();
    private final Supplier<WebDriver> provider;
    private final AtomicReference<String> baseUrl = new AtomicReference<>();

    public SeleniumContext(Supplier<WebDriver> provider) {

        Objects.requireNonNull(provider, "WebDriver must not be null");
        this.provider = provider;
    }

    /**
     * Creates a link from the other classloader to the specified context. That way it is possible to share access to
     * the same selenium context from different classloader hierarchies.
     *
     * @param context
     *         the context of the current classloader hierarchy
     * @param cl
     *         the other classloader in which the access to the given context should possible
     */
    public static void init(SeleniumContext context, ClassLoader cl) {

        if (!Objects.equals(context.getClass().getClassLoader(), cl) && context.getDriver().isPresent()) {
            try {
                final Class<?> contextClass = cl.loadClass(context.getClass().getName());
                final Constructor constr = contextClass.getConstructor(Supplier.class);
                final Supplier provider = () -> createProxy(context.getDriver().get(), cl);
                final Object ctx = constr.newInstance(provider);
                contextClass.getMethod("init").invoke(ctx);
                final String baseUrl = context.getBaseUrl();
                if (baseUrl != null) {
                    contextClass.getMethod("setBaseUrl", String.class).invoke(ctx, baseUrl);
                }
            } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException
                    | InvocationTargetException e) {
                throw new RuntimeException("Could not initialize context", e);
            }
        }
    }

    /**
     * Create a dynamic proxy that delegates invocation calls to from the other classloader hierarchy to the specified
     * webdriver. That way, a webdriver can be reused in another classloader without having to serialize it.
     *
     * @param webDriver
     *         the webdriver to create a proxy for
     * @param cl
     *         the other classloader from which access to the specified classloader should be possible
     *
     * @return a proxy for the webdriver
     */
    private static Object createProxy(final WebDriver webDriver, ClassLoader cl) {

        final List<Class> interfaces = ClassStreams.selfAndSupertypes(webDriver.getClass())
                                                   .flatMap(c -> stream(c.getInterfaces()))
                                                   .map(c -> runUnchecked(() -> cl.loadClass(c.getName())))
                                                   .collect(toList());

        return Proxy.newProxyInstance(cl,
                                      interfaces.toArray(new Class[interfaces.size()]),
                                      (proxy, method, args) -> method.invoke(webDriver, args));

    }

    public void init() {

        driver = Optional.of(provider.get());
        CONTEXT.set(Optional.of(this));
    }

    public void destroy() {

        driver.ifPresent(WebDriver::quit);
        driver = Optional.empty();
        CONTEXT.set(Optional.empty());
    }

    /**
     * Global accessor to the current context which is stored in a thread local.
     *
     * @return the context for the current thread.
     */
    public static Optional<SeleniumContext> currentContext() {

        return CONTEXT.get();
    }

    /**
     * Global accessor to the current web driver, which is stored in a thread local.
     *
     * @return the current web driver
     */
    public static Optional<WebDriver> currentDriver() {

        return currentContext().flatMap(SeleniumContext::getDriver);
    }

    /**
     * The web driver of the context
     *
     * @return a Selenium WebDriver
     */
    public Optional<WebDriver> getDriver() {

        return driver;
    }

    /**
     * The base URL for the current context used to resolve relative URLs.
     *
     * @return the current BaseUrl
     */
    public String getBaseUrl() {

        return this.baseUrl.get();
    }

    /**
     * Sets the base URL for the current context used to resolve relative URLs.
     *
     * @param baseUrl
     *         the new base URL
     */
    public void setBaseUrl(String baseUrl) {

        Objects.requireNonNull(baseUrl, "BaseUrl must not be empty");
        this.baseUrl.set(baseUrl);
    }

    /**
     * Resolves the URL path relative to the base URL.
     *
     * @param relativePath
     *         the relative path within the application
     *
     * @return the absolute path of the application's base URL and the relative path
     */
    public static String resolve(String relativePath) {

        return currentContext().map(SeleniumContext::getBaseUrl).map(base -> {
            final StringBuilder buf = new StringBuilder(16);
            buf.append(base);
            if (base.charAt(base.length() - 1) != '/') {
                buf.append('/');
            }
            if (relativePath.startsWith("/")) {
                buf.append(relativePath.substring(1));
            } else {
                buf.append(relativePath);
            }
            return buf.toString();
        }).orElse(relativePath);
    }
}

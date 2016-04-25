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

package io.tourniquet.selenium;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.tourniquet.junit.rules.ExternalResource;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;

/**
 * Context for running selenium based tests. After initialization, the context is kept as a thread local so that
 * PageObjects may access it to obtain the current state of the driver and test.
 */
public class SeleniumControl extends ExternalResource {

    private static final Logger LOG = getLogger(SeleniumControl.class);

    /**
     * The base URL for the current test execution.
     */
    private String baseUrl;

    /**
     * Action to be performed on Login
     */
    private BiConsumer<User, WebDriver> loginAction;

    /**
     * Action to be performed for logout
     */
    private Consumer<WebDriver> logoutAction;

    /**
     * Flag to indicate if login has been performed
     */
    private final AtomicBoolean loggedIn = new AtomicBoolean(false);

    /**
     * Action to initialize the web driver
     */
    private Optional<Consumer<WebDriver.Options>> driverInit;

    private Instant startTime;

    private Duration testDuration;
    private Supplier<WebDriver> driverProvider;
    /**
     * Reference to the context managed by this rule. If a context has already been initialized, this optional is empty
     */
    private Optional<SeleniumContext> managedContext;
    private boolean skipSharedDriverActions;

    @Override
    protected void before() throws Throwable {

        this.managedContext = getSeleniumContext();
        this.managedContext.ifPresent(SeleniumContext::init);
        SeleniumContext.currentContext().setBaseUrl(baseUrl);
        final WebDriver driver = SeleniumContext.currentDriver();
        driver.get(baseUrl);
        driverInit.ifPresent(di -> di.accept(driver.manage()));
        this.startTime = Instant.now();
    }

    @Override
    protected void after() {

        final Instant finishTime = Instant.now();
        this.managedContext.ifPresent(SeleniumContext::destroy);
        this.testDuration = Duration.between(this.startTime, finishTime);
        LOG.info("Test executed in {} s", this.testDuration.getSeconds());
    }

    @Override
    protected void beforeClass() throws Throwable {

        before();
    }

    @Override
    protected void afterClass() {

        after();
    }

    /**
     * Checks, if a context is already present. If that is the case, an empty optional is returned. If no context is
     * initialized, a new context is created using the driver provider of the test rule
     *
     * @return an optional containing a managed context or the empty optional if a context is already active
     */
    private Optional<SeleniumContext> getSeleniumContext() {
        try {
            SeleniumContext.currentContext();
            return Optional.empty();
        } catch (IllegalStateException e){
            return Optional.of(new SeleniumContext(driverProvider));
        }
    }

    /**
     * Performs the login action with the specified user
     *
     * @param user
     *         the user to login
     */
    public final void login(User user) {
        if(sessionActionsEnabled()) {
            loginAction.accept(user, currentDriver());
            loggedIn.set(true);
        }
    }

    /**
     * Performs the logout action
     */
    public final void logout() {
        if(sessionActionsEnabled()){
            this.logoutAction.accept(currentDriver());
            loggedIn.set(false);
        }
    }

    private boolean sessionActionsEnabled() {

        return managedContext.isPresent() || !skipSharedDriverActions;
    }

    /**
     * Indicates if a user is logged in to the application
     *
     * @return
     *  <code>true</code> if login has been performed
     */
    public boolean isLoggedIn() {

        return loggedIn.get();
    }

    /**
     * The base URL of the application to test
     *
     * @return the string representing the base URL. All relative URLs (i.e. in the page object model) must be relative
     * to this page
     */
    public String getBaseUrl() {

        return baseUrl;
    }

    /**
     * Returns the current context driver.
     *
     * @return the driver. If no context is initialized, an {@link IllegalStateException} is thrown.
     */
    public WebDriver currentDriver() {

        return SeleniumContext.currentDriver();
    }

    /**
     * Returns the current context. The context is only available during test execution
     *
     * @return the current selenium context
     */
    public SeleniumContext currentContext() {

        return SeleniumContext.currentContext();
    }

    /**
     * Returns the duration of the test execution.
     *
     * @return the duration of the test execution
     */
    public Duration getTestDuration() {

        return Optional.ofNullable(this.testDuration).orElseThrow(() -> new IllegalStateException("Test not finished"));
    }


    /**
     * Creates a new context builder for fluent setup and instantiation.
     *
     * @return a new builder
     */
    public static SeleniumControlBuilder builder() {

        return new SeleniumControlBuilder();
    }

    /**
     * Builder for creating a Selenium test context
     */
    public static class SeleniumControlBuilder {

        private Supplier<WebDriver> driverSupplier;

        private String appBaseUrl;

        private BiConsumer<User, WebDriver> appLoginAction;

        private Consumer<WebDriver> appLogoutAction;

        private Consumer<WebDriver.Options> optionsInitializer;

        private boolean skipSharedDriverActionsFlag = true;

        SeleniumControlBuilder() {
            //package private constructor to prevent direct instantiation
        }

        /**
         * Define a supplier that creates the driver. Not that the rule supports a contextual (shared) driver. In case a
         * contextual driver is present, the driver provided by this supplier is ignored.
         *
         * @param driver
         *         the driver to be used by the test
         *
         * @return this builder
         */
        public SeleniumControlBuilder driver(Supplier<WebDriver> driver) {

            this.driverSupplier = driver;
            return this;
        }

        /**
         * Specify the base URL of the application. It is the first page to be loaded for application and used to
         * resolve relative URLs
         *
         * @param baseUrl
         *         the base url of the application
         *
         * @return this builder
         */
        public SeleniumControlBuilder baseUrl(String baseUrl) {

            this.appBaseUrl = baseUrl;
            return this;
        }

        /**
         * Specify an action that should be executed to perform a login to the target application.
         *
         * @param loginAction
         *         the login action to perform
         *
         * @return this builder
         */
        public SeleniumControlBuilder loginAction(BiConsumer<User, WebDriver> loginAction) {

            this.appLoginAction = loginAction;
            return this;
        }

        /**
         * Specify an action that should be executed to perform a logout to the target application.
         *
         * @param logoutAction
         *         the logout action to perform
         *
         * @return this builder
         */
        public SeleniumControlBuilder logoutAction(Consumer<WebDriver> logoutAction) {

            this.appLogoutAction = logoutAction;
            return this;
        }

        /**
         * Specify options that should be applied on the driver after initialization.
         *
         * @param optionsInitializer
         *         the options to be applied to the driver
         *
         * @return this builder
         */
        public SeleniumControlBuilder driverOptions(Consumer<WebDriver.Options> optionsInitializer) {

            this.optionsInitializer = optionsInitializer;
            return this;
        }

        /**
         * Configure if Login and Logout Actions should be executed, if a shared (context) driver is present. Default
         * setting is true.
         *
         * @param flag
         *         the <code>true</code> if actions should be skipped for shared drivers, <code>false</code> if not
         *
         * @return this builder
         */
        public SeleniumControlBuilder skipSharedDriverActions(boolean flag) {

            this.skipSharedDriverActionsFlag = flag;
            return this;
        }

        public SeleniumControl build() {

            final SeleniumControl ctx = new SeleniumControl();
            ctx.baseUrl = this.appBaseUrl;
            ctx.driverProvider = this.driverSupplier;
            ctx.driverInit = Optional.ofNullable(this.optionsInitializer);
            ctx.loginAction = this.appLoginAction;
            ctx.logoutAction = this.appLogoutAction;
            ctx.skipSharedDriverActions = skipSharedDriverActionsFlag;
            return ctx;

        }
    }
}

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

import java.lang.reflect.Method;
import java.util.Properties;
import java.util.function.Supplier;

import org.junit.runner.Computer;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Runner for Junit tests that runs the test in a dedicated classloader.<br>
 * <b>Important:</b> this runner requires JUnit 4.12.
 */
public class JUnitRunner {

    /**
     * Executes the test using the current classloader that created the instance of the JUnit Runner. The result
     * is serialized to transfer the result across classloader boundaries.
     * @param testClass
     *  the class to be executed
     * @return
     *  a binary representation of the {@link org.junit.runner.Result}
     * @throws Exception
     *  if the test execution or result serialization failed.
     */
    public byte[] run(Class<?> testClass) throws Exception{
        final Computer computer = new Computer();
        final JUnitCore core = new JUnitCore();
        return new ResultHelper().serialize(core.run(computer, testClass));
    }

    /**
     * Executes the test using the provided classloader. The is loaded and executed using the specified
     * classloader. The method ensures, that the result
     * @param className
     *  the name of the test class or suite to be executed
     * @param classLoader
     *  the classloader that should be used to run the test.
     * @return
     *  the result of the test execution. Note that in case the test threw an exception, the result will contain
     *  only a generic replica of the original exception, including message and stacktrace.
     * @throws Exception
     *  if the test could not be executed or result retrieved.
     */
    public static Result runClass(String className, Supplier<ClassLoader> classLoader) throws Exception {
        final Properties origProps = new Properties();
        origProps.putAll(System.getProperties());
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        final ClassLoader cl = classLoader.get();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            final Class<?> testClass = cl.loadClass(className);
            final Object runner = cl.loadClass(JUnitRunner.class.getName()).newInstance();
            final Method run = runner.getClass().getDeclaredMethod("run", Class.class);
            return new ResultHelper().deserialize((byte[])run.invoke(runner, testClass));
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
            final Properties currentProps = System.getProperties();
            currentProps.clear();
            currentProps.putAll(origProps);
        }
    }

}

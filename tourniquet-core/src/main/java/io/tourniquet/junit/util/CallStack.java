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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * Helper class for operations on the call stack Created by Gerald Muecke on 26.11.2015.
 */
public final class CallStack {

    private static final String THIS_NAME = CallStack.class.getName();

    private CallStack() {

    }

    private static ThreadLocal<Deque<Method>> METHODSTACK = ThreadLocal.withInitial(() -> new ArrayDeque<>());

    /**
     * Enhances the target object to track the invocation of methods of the target. The current method can
     * be retrieved by invoking {@link #currentMethod()}
     * @param target
     *  the target object whose method invocations should be tracked
     * @param <T>
     *      the type of the tracked object
     * @return
     *  a tracked object whose method references will be put onto a stack so that the most recent method can
     *  be retrieved using the {@link #currentMethod()} call
     *
     */
    public static <T> T track(T target){

        //noinspection unchecked
        return (T) Enhancer.create(target.getClass(), (MethodInterceptor) (o, method, objects, methodProxy) -> {
            METHODSTACK.get().push(method);
            try{

                return method.invoke(target, objects);
            } finally {
                METHODSTACK.get().pop();
            }

        });
    }

    /**
     * Returns a reference to a method that was last invoked on a target object which has been enhanced for method
     * recording. This method only works properly if the caller of this method is tracked.
     * @return
     *  the current method on the tracked call stack or an empty optional if no method was found, i.e. because no
     *  object on the actual callstack is tracked.
     */
    public static Optional<Method> currentMethod() {
        return Optional.ofNullable(METHODSTACK.get().peek());
    }

    /**
     * Returns the caller class of the calling method.<br> For example: <br> A.calling() -&gt; B.called() B.called()
     * -&gt;
     * getCallerClass(): A <br> If a thread context classloader is defined, it will be used for loading the class,
     * otherwise the default class loader is used.
     *
     * @return the class of the calling method's class
     */
    public static Class<?> getCallerClass() {

        final StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        try {
            final StackTraceElement caller = findCaller(stElements);
            return loadClass(caller.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not determine caller class", e);
        }
    }

    private static StackTraceElement findCaller(final StackTraceElement[] stElements) { //NOSONAR
        //we start with 1 as 0 is always java.lang.Thread itself
        for (int i = 1,
             len = stElements.length - 1; i < len; i++) {
            final StackTraceElement ste = stElements[i];
            if (THIS_NAME.equals(ste.getClassName())) {
                continue;
            } else if (stElements[i + 1].getMethodName().matches("access\\$\\d+")) {
                //there might be an accessor method between, ignore it
                return stElements[i + 2];
            } else {
                //we dont want the caller of the getCallerClass Method (i) but the caller of
                //the method which call getCallerClass (i+1)
                return stElements[i + 1];
            }

        }
        return stElements[stElements.length - 1];
    }

    /**
     * Loads the class specified by name using the Thread's context class loader - if defined - otherwise the default
     * classloader.
     *
     * @param className
     *         the name of the class to load
     *
     * @return the loaded class
     *
     * @throws ClassNotFoundException
     *         if the the class could not be loaded
     */
    private static Class<?> loadClass(final String className) throws ClassNotFoundException {

        ClassLoader ctxCL = Thread.currentThread().getContextClassLoader();
        if (ctxCL == null) {
            return Class.forName(className);
        }
        return ctxCL.loadClass(className);
    }

}

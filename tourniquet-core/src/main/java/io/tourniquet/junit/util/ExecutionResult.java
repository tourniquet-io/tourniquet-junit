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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Monad for the result of a method execution. As every execution may result in
 * <ul>
 *     <li>a value</li>
 *     <li>null</li>
 *     <li>an exception</li>
 * </ul>
 * this wrapper is capable of dealing with all three. Apart from accessors to the result of the operation, it provides
 * methods for dealing with exception, like catching them, map and rethrow them and do final tasks.
 */
public class ExecutionResult<RESULTTYPE> {

    /**
     * Constant representing a void return result for being used as result of Runnable executions.
     */
    public static final ExecutionResult<Void> VOID = new ExecutionResult<>(null, null);

    private final Optional<RESULTTYPE> result;
    private final Optional<Exception> exception;

    protected ExecutionResult(RESULTTYPE result, Exception exception) {
        this.result = Optional.ofNullable(result);
        this.exception = Optional.ofNullable(exception);
    }

    /**
     * Creates a result representing a void operation result
     * @return
     *  the void result
     */
    public static ExecutionResult<Void> ofVoid() {
        return VOID;
    }

    /**
     * Constructor for successful outcome of an operation
     * @param result
     *  the return value of the operation
     */
    public static <RESULTTYPE> ExecutionResult<RESULTTYPE> ofSuccess(RESULTTYPE result) {
        return new ExecutionResult<>(result, null);
    }

    /**
     * Constructor for exceptional outcome of an operation
     * @param exception
     *  the caught exception
     */
    public static <RESULTTYPE> ExecutionResult<RESULTTYPE> ofException(Exception exception) {
        return new ExecutionResult<>(null, exception);
    }

    /**
     * Checks if there were any exception during the execution
     * @return
     *  true, if there were no exceptions
     */
    public boolean wasSuccess() {

        return !this.exception.isPresent();
    }

    /**
     * Returns the result of the operation. If an exception occurred or null was return, this method will return
     * the empty optional
     * @return
     *  an optional of the return value
     */
    public Optional<RESULTTYPE> getReturnValue() {

        return result;
    }

    /**
     * Returns the result of the operation. May be null.
     * @return
     *  the actual value of the operation
     */
    public RESULTTYPE get() {

        return this.result.orElse(null);
    }

    /**
     * Flattens the execution result to the actual outcome of the execution. If an exception occured, it is throw,
     * if it returned any result - including null - it is returned.
     * @return
     *  the actual return value of the operation
     * @throws Exception
     *  if the execution produced an exception, it is thrown
     */
    public RESULTTYPE flatten() throws Exception { //NOSONAR

        if (!wasSuccess()) {
            throw this.exception.get();
        }
        return get();
    }

    /**
     * Returns the exception of the operation or the empty optional, if no exception occurred.
     * @return
     *  the exception caught during the execution
     */
    public Optional<Exception> getException() {

        return exception;
    }

    /**
     * Task that is run in any case, exception or not. This method can be used to model {@code finally} blocks
     * @param finalTask
     *  final task to be executed.
     * @return
     *  this result
     */
    public ExecutionResult<RESULTTYPE> doFinally(Runnable finalTask) { //NOSONAR

        finalTask.run(); //NOSONAR
        return this;
    }

    /**
     * Method to fluently process the return value of the operation.
     * @param consumer
     *  the consumer to process the return value
     * @return
     *  this measure
     */
    public ExecutionResult<RESULTTYPE> forReturnValue(Consumer<RESULTTYPE> consumer){
        result.ifPresent(consumer::accept);
        return this;
    }

    /**
     * Method to process the exception of the operation regardless of the exception's type.
     * @param consumer
     *  the consumer to process the exceptional throwable
     * @return
     *  this measure
     */
    public ExecutionResult<RESULTTYPE> catchException(Consumer<Throwable> consumer){
        exception.ifPresent(consumer::accept);
        return this;
    }

    /**
     * Handler for a caught exception. This is useful for catching an exception without rethrowing them.
     * @param exceptionType
     *  the exception type to be handled
     * @param handler
     *  the handler that processes the execution
     * @param <X>
     *      the type of the exception to handle
     * @return
     *  this result
     */
    @SuppressWarnings("unchecked")
    public <X extends Exception> ExecutionResult<RESULTTYPE> catchException(Class<X> exceptionType,
                                                                            Consumer<X> handler) {

        this.exception.ifPresent(x -> {
            if (exceptionType.isAssignableFrom(x.getClass())) {
                handler.accept((X) x);
            }
        });
        return this;
    }

    /**
     * Maps the exception to another exception type and throws the result as new exception.
     * @param exceptionType
     *  the exception type to be handled
     * @param mapper
     *  the mapper function for converting the caught exception to the target exception
     * @param <ORIGINAL>
     *   the type of the original exception
     * @param <RETHROW>
     *   the type the new exception
     * @return
     * @throws RETHROW
     *  the mapped exception, if the exception of the execution matched the specified type.
     */
    @SuppressWarnings("unchecked")
    public <ORIGINAL extends Exception, RETHROW extends Exception> ExecutionResult<RESULTTYPE> mapException(
            Class<ORIGINAL> exceptionType,
            Function<ORIGINAL, RETHROW> mapper) throws RETHROW{

        if(this.exception.isPresent() && exceptionType.isAssignableFrom(this.exception.get().getClass())){
            throw mapper.apply((ORIGINAL) this.exception.get()); //NOSONAR
        }
        return this;
    }
}

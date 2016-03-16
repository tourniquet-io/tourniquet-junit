package io.tourniquet.junit.util;

import java.util.concurrent.Callable;

/**
 * Helper to perform tasks in a functional way without having to explicitly handle exceptions.
 */
public final class ExecutionHelper {

    private ExecutionHelper(){}

    /**
     * Executes a specified task, regardless if it throws a checked or unchecked exception.
     * If an exception occurs it will be caught and wrapped in the execution result.
     * @param call
     *  the task to be executed
     * @param <T>
     *      the type of the return value
     * @return
     *  the result of the task execution
     */
    public static <T> T runUnchecked(Callable<T> call) {
        return runProtected(call).mapException(Exception.class, RuntimeException::new).get();
    }

    /**
     * Executes a specified task. If an exception occurs it will be wrapped in a RuntimeException
     * @param runnable
     *  the task to be executed
     * @return
     *  the result of the task execution
     */
    public static void runUnchecked(Runnable runnable) {
        runProtected(runnable).mapException(Exception.class, RuntimeException::new).get();
    }

    /**
     * Executes the specified task and returnes an execution result.
     * @param callable
     *  the task to be executed
     * @param <RESULTTYPE>
     *  the return type of the operation
     * @return
     *  the execution result monad
     */
    public static <RESULTTYPE> ExecutionResult<RESULTTYPE> runProtected(Callable<RESULTTYPE> callable) {
        try {
            return ExecutionResult.ofSuccess(callable.call());
        } catch (Exception e) {
            return ExecutionResult.ofException(e);
        }
    }

    /**
     * Executes the specified task, handling a any exception that occurs.
     * @param runnable
     *  the task to be executed
     * @return
     *  the execution result monad
     */
    public static ExecutionResult<Void> runProtected(Runnable runnable) {
        try {
            runnable.run();
            return ExecutionResult.ofVoid();
        } catch (Exception e) {
            return ExecutionResult.ofException(e);
        }
    }

}

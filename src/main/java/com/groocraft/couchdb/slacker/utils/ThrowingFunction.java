package com.groocraft.couchdb.slacker.utils;

/**
 * {@link FunctionalInterface} which is providing ability to pass exceptional state from {@link #apply(Object)} method. It can be handful when we need to
 * pass some logic into method which is already prepared to handle exceptional state of the same type and avoid to using {@link RuntimeException}
 *
 * @param <ParameterT> type of parameter passed into the function
 * @param <ReturnedT>  type of return from the function
 * @param <ExceptionT> type of {@link Exception} which can be thrown from the function
 * @author Majlanky
 */
@FunctionalInterface
public interface ThrowingFunction<ParameterT, ReturnedT, ExceptionT extends Exception> {

    /**
     * Applies this function to the given argument.
     *
     * @param parameter the function argument
     * @return the function result
     * @throws ExceptionT
     */
    ReturnedT apply(ParameterT parameter) throws ExceptionT;

}

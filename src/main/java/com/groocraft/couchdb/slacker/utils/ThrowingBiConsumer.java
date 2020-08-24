package com.groocraft.couchdb.slacker.utils;

/**
 * Class providing consumer functionality for two values
 *
 * @param <FirstParameterT>    Type of first parameter consumed
 * @param <TheOtherParameterT> Type of the other parameter consumed
 * @param <ExceptionT>         Type of {@link Exception} which can be thrown during consummation
 * @author Majlanky
 */
@FunctionalInterface
public interface ThrowingBiConsumer<FirstParameterT, TheOtherParameterT, ExceptionT extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param first    parameter of consumer
     * @param theOther parameter of consumer
     * @throws ExceptionT if implementation inside throw exception
     */
    void accept(FirstParameterT first, TheOtherParameterT theOther) throws ExceptionT;

}

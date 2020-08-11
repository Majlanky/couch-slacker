package com.groocraft.couchdb.slacker.utils;

@FunctionalInterface
public interface ThrowingConsumer<DataT, ExceptionT extends Exception> {

    /**
     * Performs this operation on the given argument.
     *
     * @param data the input argument
     */
    void accept(DataT data) throws ExceptionT;

}

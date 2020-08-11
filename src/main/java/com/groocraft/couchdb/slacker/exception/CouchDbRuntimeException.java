package com.groocraft.couchdb.slacker.exception;

/**
 * Wrapper of all exceptions thrown during a work with CouchDB from Spring Data.
 *
 * @author Majlanky
 */
public class CouchDbRuntimeException extends RuntimeException {

    public CouchDbRuntimeException(String message) {
        super(message);
    }

    public CouchDbRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

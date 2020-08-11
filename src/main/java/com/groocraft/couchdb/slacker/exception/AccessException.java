package com.groocraft.couchdb.slacker.exception;

/**
 * Exception used during states caused by wrong access implementation/configuration in {@link com.groocraft.couchdb.slacker.data.Reader} and
 * {@link com.groocraft.couchdb.slacker.data.Writer} implementations. The likeliest use-cases are tries to access non-existing field, method or accessing
 * method with wrong number or type parameters.
 *
 * @author Majlanky
 */
public class AccessException extends RuntimeException {

    public AccessException(Throwable cause) {
        super(cause);
    }
}

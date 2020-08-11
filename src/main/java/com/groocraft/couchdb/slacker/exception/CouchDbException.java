package com.groocraft.couchdb.slacker.exception;

import java.io.IOException;

public class CouchDbException extends IOException {

    private final int statusCode;

    public CouchDbException(int statusCode, String method, String uri, String reason) {
        super(String.format("%s when trying %s on %s with reason %s", statusCode, method, uri, reason));
        this.statusCode = statusCode;

    }

    public int getStatusCode(){
        return statusCode;
    }

}

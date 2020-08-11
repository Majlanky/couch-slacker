package com.groocraft.couchdb.slacker.exception;

import java.util.Map;
import java.util.stream.Collectors;

public class CouchDBBulkOperationException extends RuntimeException {

    private final Map<Object, Exception> failed;
    private final String operation;

    public <EntityT> CouchDBBulkOperationException(String operation, Map<EntityT, Exception> failed) {
        super(formatMessage(operation, failed));
        this.failed = (Map<Object, Exception>)failed;
        this.operation = operation;
    }

    private static <EntityT> String formatMessage(String operation, Map<EntityT, Exception> failed) {
        return "The following failed to " + operation + ": " + failed.entrySet().stream().map(e -> e.getKey() + " cause of " + e.getValue()).collect(Collectors.joining(", "));
    }

    public Map<Object, Exception> getFailed() {
        return failed;
    }

    public String getOperation() {
        return operation;
    }
}

package com.groocraft.couchdb.slacker.utils;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public class ThrowingStreamResult<ProcessedT, FailedT> {

    private final Map<FailedT, Exception> failed;
    private final List<ProcessedT> processed;

    private ThrowingStreamResult(List<ProcessedT> processed, Map<FailedT, Exception> failed) {
        this.failed = failed;
        this.processed = processed;
    }

    public static <ProcessedT, FailedT> ThrowingStreamResult<ProcessedT, FailedT> of(List<ProcessedT> processed, Map<FailedT, Exception> failed) {
        return new ThrowingStreamResult<>(processed, failed);
    }

    public <RuntimeExceptionT extends RuntimeException> Stream<ProcessedT> throwIfUnprocessed(Function<Map<FailedT, Exception>, RuntimeExceptionT> f){
        if(failed.size() > 0) {
            throw f.apply(failed);
        }
        return processed.stream();
    }
}

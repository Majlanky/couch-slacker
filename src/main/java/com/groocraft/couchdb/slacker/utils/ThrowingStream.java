package com.groocraft.couchdb.slacker.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ThrowingStream<InputT, OutputT> {

    private final Iterable<InputT> iterable;

    private ThrowingStream(Iterable<InputT> iterable) {
        this.iterable = iterable;
    }

    public static <InputT, Output> ThrowingStream<InputT, Output> of(Iterable<InputT> iterable) {
        return new ThrowingStream<>(iterable);
    }

    public <ExceptionT extends Exception> ThrowingStreamResult<OutputT, InputT> forEach(ThrowingFunction<InputT, OutputT, ExceptionT> action) {
        Map<InputT, Exception> failed = new HashMap<>();
        List<OutputT> processed = new LinkedList<>();
        for (InputT data : iterable) {
            try {
                processed.add(action.apply(data));
            } catch (Exception e) {
                failed.put(data, e);
            }
        }
        return ThrowingStreamResult.of(processed, failed);
    }

}

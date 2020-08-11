package com.groocraft.couchdb.slacker.utils;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

//TODO erase
public class IterableStream<DataT> implements Iterable<DataT> {

    private final Stream<DataT> stream;

    private IterableStream(Stream<DataT> stream) {
        this.stream = stream;
    }

    public static <DataT> IterableStream<DataT> of(Stream<DataT> stream){
        return new IterableStream<>(stream);
    }

    @Override
    public Iterator<DataT> iterator() {
        return stream.iterator();
    }

    @Override
    public void forEach(Consumer<? super DataT> action) {
        stream.forEach(action);
    }

    @Override
    public Spliterator<DataT> spliterator() {
        return stream.spliterator();
    }

}

package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IterableStreamTest {

    @Mock
    Stream<String> stream;

    @Mock
    Spliterator<String> spliterator;

    @Mock
    Iterator<String> iterator;


    @Test
    public void testIterable(){
        when(stream.spliterator()).thenReturn(spliterator);
        when(stream.iterator()).thenReturn(iterator);
        IterableStream is = IterableStream.of(stream);
        assertEquals(spliterator, is.spliterator(), "Spliterator of the original stream must be used");
        assertEquals(iterator, is.iterator(), "Iterator of the original stream must be used");
    }

    @Test
    public void testForEach(){
        List<String> processed = new LinkedList<>();
        IterableStream<String> is = IterableStream.of(List.of("1", "2", "3").stream());
        is.forEach(s -> processed.add(s));
        assertEquals(3, processed.size(), "Not all of members of original stream was processed");
        assertTrue(processed.contains("1"), "One of element from original stream is missing");
        assertTrue(processed.contains("2"), "One of element from original stream is missing");
        assertTrue(processed.contains("3"), "One of element from original stream is missing");
    }

}
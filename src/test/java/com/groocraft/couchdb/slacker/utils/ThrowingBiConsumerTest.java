package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThrowingBiConsumerTest {

    @Test
    void test() {
        assertThrows(Exception.class, () -> process(this::processString), "Exception produces in the function must be passed outside");
    }

    private void processString(String s, String s2) throws IOException {
        throw new IOException(s + s2);
    }

    private void process(ThrowingBiConsumer<String, String, Exception> f) throws Exception {
        f.accept("test", "test2");
    }

}
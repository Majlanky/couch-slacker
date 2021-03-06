package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ThrowingFunctionTest {

    @Test
    void test() {
        assertThrows(Exception.class, () -> process(this::processString), "Exception produces in the function must be passed outside");
    }

    private String processString(String s) throws IOException {
        throw new IOException(s);
    }

    private void process(ThrowingFunction<String, String, Exception> f) throws Exception {
        f.apply("test");
    }

}
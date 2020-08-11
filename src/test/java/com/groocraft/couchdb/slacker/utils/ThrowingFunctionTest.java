package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ThrowingFunctionTest {

    @Test
    public void test(){
        assertThrows(Exception.class, () -> {
            process(this::processString);
            assertTrue(Thread.currentThread().isInterrupted(), "Thread interrupted flag must stay on true");
        },"Exception produces in the function must be passed outside");
    }

    private String processString(String s) throws IOException{
        throw new IOException(s);
    }

    private String process(ThrowingFunction<String, String, Exception> f) throws Exception{
        return f.apply("test");
    }

}
package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodReaderTest {

    private boolean called = false;
    private String value = "test";

    @Test
    public void test() throws NoSuchMethodException {
        MethodReader<String> reader = new MethodReader<>(MethodReaderTest.class.getDeclaredMethod("read"));
        String read = reader.read(this);
        assertTrue(called, "The wrapped method was not called");
        assertEquals(value, read, "Read value is not matching to value returned by wrapped method");
    }

    @Test
    public void testIllegalAccessWrite() throws IllegalAccessException, InvocationTargetException {
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.invoke(Mockito.any())).thenThrow(new IllegalAccessException());
        MethodReader<String> reader = new MethodReader<>(method);
        assertThrows(AccessException.class, () -> reader.read(this), "Illegal access must be reported as AccessException");
    }

    @Test
    public void testInvocationWrite() throws IllegalAccessException, InvocationTargetException {
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.invoke(Mockito.any())).thenThrow(new InvocationTargetException(new Exception()));
        MethodReader<String> reader = new MethodReader<>(method);
        assertThrows(AccessException.class, () -> reader.read(this), "Invocation issues must be reported as AccessException");
    }

    public String read() {
        called = true;
        return value;
    }

}
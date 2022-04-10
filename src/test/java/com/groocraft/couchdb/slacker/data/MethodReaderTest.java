/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    private final String value = "test";

    @Test
    void test() throws NoSuchMethodException {
        MethodReader<String> reader = new MethodReader<>(MethodReaderTest.class.getDeclaredMethod("read"));
        String read = reader.read(this);
        assertTrue(called, "The wrapped method was not called");
        assertEquals(value, read, "Read value is not matching to value returned by wrapped method");
    }

    @Test
    void testIllegalAccessWrite() throws IllegalAccessException, InvocationTargetException {
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.invoke(Mockito.any())).thenThrow(new IllegalAccessException());
        MethodReader<String> reader = new MethodReader<>(method);
        assertThrows(AccessException.class, () -> reader.read(this), "Illegal access must be reported as AccessException");
    }

    @Test
    void testInvocationWrite() throws IllegalAccessException, InvocationTargetException {
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
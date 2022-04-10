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

class MethodWriterTest {

    private boolean called = false;
    private String value = "test";

    @Test
    void test() throws NoSuchMethodException {
        MethodWriter<String> writer = new MethodWriter<>(MethodWriterTest.class.getDeclaredMethod("write", String.class));
        String newValue = "new";
        writer.write(this, newValue);
        assertTrue(called, "The wrapped method was not called");
        assertEquals(newValue, value, "The current value is not matching to the value written by the writer");
    }

    @Test
    void testIllegalAccessWrite() throws IllegalAccessException, InvocationTargetException {
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.invoke(Mockito.any(), Mockito.any())).thenThrow(new IllegalAccessException());
        MethodWriter<String> writer = new MethodWriter<>(method);
        assertThrows(AccessException.class, () -> writer.write(this, "something"), "Illegal access must be reported as AccessException");
    }

    @Test
    void testInvocationWrite() throws IllegalAccessException, InvocationTargetException {
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.invoke(Mockito.any(), Mockito.any())).thenThrow(new InvocationTargetException(new Exception()));
        MethodWriter<String> writer = new MethodWriter<>(method);
        assertThrows(AccessException.class, () -> writer.write(this, "something"), "Invocation issues must be reported as AccessException");
    }

    void write(String value) {
        called = true;
        this.value = value;
    }

}
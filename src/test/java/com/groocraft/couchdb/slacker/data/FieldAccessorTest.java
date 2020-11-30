package com.groocraft.couchdb.slacker.data;

import com.groocraft.couchdb.slacker.exception.AccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldAccessorTest {

    private FieldAccessor<String> accessor;
    private String field;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        field = "initial";
        accessor = new FieldAccessor<>(FieldAccessorTest.class.getDeclaredField("field"));
    }

    @Test
    void testRead() {
        assertEquals(field, accessor.read(this), "Actual value of the accessed field was read improperly");
    }

    @Test
    void testWrite() {
        String newValue = "new";
        accessor.write(this, newValue);
        assertEquals(newValue, field, "Actual value of the accessed field do not match the thru accessor written value");
    }

    @Test
    void testIllegalAccessRead() throws IllegalAccessException {
        Field field = Mockito.mock(Field.class);
        Mockito.when(field.get(Mockito.any())).thenThrow(new IllegalAccessException());
        FieldAccessor<String> accessor = new FieldAccessor<>(field);
        assertThrows(AccessException.class, () -> accessor.read(this), "Illegal access must be reported as AccessException");
    }

    @Test
    void testIllegalAccessWrite() throws IllegalAccessException {
        Field field = Mockito.mock(Field.class);
        Mockito.doThrow(new IllegalAccessException()).when(field).set(Mockito.any(), Mockito.any());
        FieldAccessor<String> accessor = new FieldAccessor<>(field);
        assertThrows(AccessException.class, () -> accessor.write(this, "something"), "Illegal access must be reported as AccessException");
    }

}
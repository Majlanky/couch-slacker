package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.data.repository.query.parser.Part;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OperationTest {

    @Test
    public void test() {
        assertAll(Arrays.stream(Part.Type.values()).filter(t -> !Part.Type.EXISTS.equals(t)).map(this::executable));
    }

    private Executable executable(Part.Type type) {
        return () -> assertDoesNotThrow(() -> Operation.of(type), "There is no equivalent for " + type);
    }
}
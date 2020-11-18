package com.groocraft.couchdb.slacker.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.data.repository.query.parser.Part;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationTest {

    final static String VIEW_MAP = "function(doc){if%1$s{emit(null);}}";

    @Test
    public void testJs() {
        assertEquals("function(doc){if(doc.data == \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.EQUALS.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data == 1){emit(null);}}", String.format(VIEW_MAP, Operation.EQUALS.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data != \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_EQUALS.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data != 1){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_EQUALS.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data > \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data > 1){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data >= \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN_OR_EQUALS.jsCondition("data",
                "STR")));
        assertEquals("function(doc){if(doc.data >= 1){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN_OR_EQUALS.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data < \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data < 1){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data <= \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN_OR_EQUALS.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data <= 1){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN_OR_EQUALS.jsCondition("data", 1)));

        assertEquals("function(doc){if(/STR/.test(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.REGEX.jsCondition("data", "STR")));

        assertEquals("function(doc){if(doc.data != null){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_NULL.jsCondition("data", 1)));
        assertEquals("function(doc){if(doc.data == null){emit(null);}}", String.format(VIEW_MAP, Operation.NULL.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data < \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.BEFORE.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data < 1){emit(null);}}", String.format(VIEW_MAP, Operation.BEFORE.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data > \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.AFTER.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data > 1){emit(null);}}", String.format(VIEW_MAP, Operation.AFTER.jsCondition("data", 1)));

        assertEquals("function(doc){if(doc.data.startsWith(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.STARTING_WITH.jsCondition("data",
                "STR")));
        assertEquals("function(doc){if(doc.data.endsWith(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.ENDING_WITH.jsCondition("data", "STR")));

        assertEquals("function(doc){if(doc.data.length == 0){emit(null);}}", String.format(VIEW_MAP, Operation.EMPTY.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data.length != 0){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_EMPTY.jsCondition("data", "STR")));

        assertEquals("function(doc){if(doc.data.includes(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.CONTAINING.jsCondition("data", "STR")));
        assertEquals("function(doc){if(!doc.data.includes(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_CONTAINING.jsCondition("data",
                "STR")));

        assertEquals("function(doc){if(doc.data.startsWith(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.LIKE.jsCondition("data", "STR")));
        assertEquals("function(doc){if(!doc.data.includes(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_LIKE.jsCondition("data", "STR")));

        assertEquals("function(doc){if([\"1\",\"2\",\"3\"].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.IN.jsCondition("data",
                Arrays.asList("1", "2", "3"))));
        assertEquals("function(doc){if(![\"1\",\"2\",\"3\"].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_IN.jsCondition("data",
                Arrays.asList("1", "2", "3"))));

        assertEquals("function(doc){if([1,2,3].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.IN.jsCondition("data",
                Arrays.asList(1, 2, 3))));
        assertEquals("function(doc){if(![1,2,3].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_IN.jsCondition("data",
                Arrays.asList(1, 2, 3))));

        assertEquals("function(doc){if(doc.data == true){emit(null);}}", String.format(VIEW_MAP, Operation.TRUE.jsCondition("data", "STR")));
        assertEquals("function(doc){if(doc.data == false){emit(null);}}", String.format(VIEW_MAP, Operation.FALSE.jsCondition("data", "STR")));
    }

    @Test
    public void test() {
        assertAll(Arrays.stream(Part.Type.values()).filter(t -> !Part.Type.EXISTS.equals(t)).map(this::executable));
    }

    private Executable executable(Part.Type type) {
        return () -> assertDoesNotThrow(() -> Operation.of(type), "There is no equivalent for " + type);
    }
}
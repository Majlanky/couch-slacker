package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    void testJs() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String str = "STR";
        assertEquals("function(doc){if(doc.data == \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.EQUALS.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data == 1){emit(null);}}", String.format(VIEW_MAP, Operation.EQUALS.jsCondition("data", 1, mapper)));
        assertEquals("function(doc){if(JSON.stringify(doc.data) == JSON.stringify([\"1\",\"2\",\"3\"])){emit(null);}}",
                String.format(VIEW_MAP, Operation.EQUALS.jsCondition("data", Arrays.asList("1", "2", "3"), mapper)));

        assertEquals("function(doc){if(doc.data != \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_EQUALS.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data != 1){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_EQUALS.jsCondition("data", 1, mapper)));
        assertEquals("function(doc){if(JSON.stringify(doc.data) != JSON.stringify([\"1\",\"2\",\"3\"])){emit(null);}}",
                String.format(VIEW_MAP, Operation.NOT_EQUALS.jsCondition("data", Arrays.asList("1", "2", "3"), mapper)));

        assertEquals("function(doc){if(doc.data > \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data > 1){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN.jsCondition("data", 1, mapper)));

        assertEquals("function(doc){if(doc.data >= \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN_OR_EQUALS.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data >= 1){emit(null);}}", String.format(VIEW_MAP, Operation.GREATER_THAN_OR_EQUALS.jsCondition("data", 1, mapper)));

        assertEquals("function(doc){if(doc.data < \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data < 1){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN.jsCondition("data", 1, mapper)));

        assertEquals("function(doc){if(doc.data <= \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN_OR_EQUALS.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data <= 1){emit(null);}}", String.format(VIEW_MAP, Operation.LESSER_THAN_OR_EQUALS.jsCondition("data", 1, mapper)));

        assertEquals("function(doc){if(/STR/.test(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.REGEX.jsCondition("data", str, mapper)));

        assertEquals("function(doc){if(doc.data != null){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_NULL.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data == null){emit(null);}}", String.format(VIEW_MAP, Operation.NULL.jsCondition("data", str, mapper)));

        assertEquals("function(doc){if(doc.data < \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.BEFORE.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data < 1){emit(null);}}", String.format(VIEW_MAP, Operation.BEFORE.jsCondition("data", 1, mapper)));

        assertEquals("function(doc){if(doc.data > \"STR\"){emit(null);}}", String.format(VIEW_MAP, Operation.AFTER.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data > 1){emit(null);}}", String.format(VIEW_MAP, Operation.AFTER.jsCondition("data", 1, mapper)));

        assertEquals("function(doc){if(doc.data.startsWith(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.STARTING_WITH.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data.endsWith(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.ENDING_WITH.jsCondition("data", str, mapper)));

        assertEquals("function(doc){if(doc.data.length == 0){emit(null);}}", String.format(VIEW_MAP, Operation.EMPTY.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data.length != 0){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_EMPTY.jsCondition("data", str, mapper)));

        assertEquals("function(doc){if(doc.data.includes(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.CONTAINING.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(!doc.data.includes(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_CONTAINING.jsCondition("data", str, mapper)));

        assertEquals("function(doc){if(doc.data.startsWith(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.LIKE.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(!doc.data.includes(\"STR\")){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_LIKE.jsCondition("data", str, mapper)));

        assertEquals("function(doc){if([\"1\",\"2\",\"3\"].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.IN.jsCondition("data",
                Arrays.asList("1", "2", "3"), mapper)));
        assertEquals("function(doc){if(![\"1\",\"2\",\"3\"].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_IN.jsCondition("data",
                Arrays.asList("1", "2", "3"), mapper)));

        assertEquals("function(doc){if([1,2,3].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.IN.jsCondition("data",
                Arrays.asList(1, 2, 3), mapper)));
        assertEquals("function(doc){if(![1,2,3].includes(doc.data)){emit(null);}}", String.format(VIEW_MAP, Operation.NOT_IN.jsCondition("data",
                Arrays.asList(1, 2, 3), mapper)));

        assertEquals("function(doc){if(doc.data == true){emit(null);}}", String.format(VIEW_MAP, Operation.TRUE.jsCondition("data", str, mapper)));
        assertEquals("function(doc){if(doc.data == false){emit(null);}}", String.format(VIEW_MAP, Operation.FALSE.jsCondition("data", str, mapper)));
    }

    @Test
    void test() {
        assertAll(Arrays.stream(Part.Type.values()).filter(t -> !Part.Type.EXISTS.equals(t)).map(this::executable));
    }

    private Executable executable(Part.Type type) {
        return () -> assertDoesNotThrow(() -> Operation.of(type), "There is no equivalent for " + type);
    }
}
package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.groocraft.couchdb.slacker.structure.AllDocumentResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllDocsIdDeserializerTest {

    @Test
    void test() throws JsonProcessingException {
        String json = "{\"total_rows\":2,\"offset\":0,\"rows\":[{\"id\":\"a\",\"key\":\"a\",\"value\":{\"rev\":\"revA\"}},{\"id\":\"b\",\"key\":\"b\",\"value\":{\"rev\":\"revB\"}}]}";
        ObjectMapper mapper = new ObjectMapper();
        AllDocumentResponse response = mapper.readValue(json, AllDocumentResponse.class);
        assertEquals(2, response.getRows().size(), "Json contains 2 ids");
        assertEquals("a", response.getRows().get(0), "First id in json is a");
        assertEquals("b", response.getRows().get(1), "First id in json is b");
        assertEquals(2, response.getTotalRows(), "Json reports 2 as total");
        assertEquals(0, response.getOffset(), "Json reports 0 as offset");
    }

}
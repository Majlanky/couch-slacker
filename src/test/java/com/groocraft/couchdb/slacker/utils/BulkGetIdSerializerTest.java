package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.structure.BulkRequest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkGetIdSerializerTest {

    @Test
    public void test() throws JsonProcessingException {
        EntityMetadata<TestDocument> metadata = new EntityMetadata<>(TestDocument.class);
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new BulkGetIdSerializer<>(TestDocument.class, metadata.getIdReader()));
        localMapper.registerModule(module);
        assertEquals("{\"docs\":[{\"id\":\"a\"},{\"id\":\"b\"}]}",
                localMapper.writeValueAsString(
                        new BulkRequest<>(Arrays.asList(
                                new TestDocument("a", "revA", "a"),
                                new TestDocument("b", "revB", "b")))));
    }

}
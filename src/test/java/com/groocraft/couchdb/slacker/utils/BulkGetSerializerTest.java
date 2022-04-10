package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.groocraft.couchdb.slacker.DocumentDescriptor;
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.structure.BulkGetRequest;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkGetSerializerTest {

    @Test
    void test() throws JsonProcessingException {
        EntityMetadata metadata = new EntityMetadata(DocumentDescriptor.of(TestDocument.class));
        ObjectMapper localMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        localMapper.registerModule(module);
        assertEquals("{\"docs\":[{\"id\":\"a\"},{\"id\":\"b\"}]}",
                localMapper.writeValueAsString(
                        new BulkGetRequest(Arrays.asList("a", "b"))));
    }

}
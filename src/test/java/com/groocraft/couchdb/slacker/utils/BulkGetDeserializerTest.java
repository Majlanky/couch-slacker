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

package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.groocraft.couchdb.slacker.structure.BulkGetResponse;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BulkGetDeserializerTest {

    @Test
    void test() throws JsonProcessingException {
        String json = "{\"results\": [{\"id\": \"a\", \"docs\": [{\"ok\":{\"_id\":\"a\",\"_rev\":\"revA\",\"value\":\"valueA\",\"value2\":\"value2a\"}}]},{\"id\": \"b\", \"docs\": [{\"ok\":{\"_id\":\"b\",\"_rev\":\"revB\",\"value\":\"valueB\",\"value2\":\"value2b\"}}]}]}";
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(List.class, new BulkGetDeserializer<>(TestDocument.class));
        mapper.registerModule(module);
        BulkGetResponse<TestDocument> response = mapper.readValue(json, mapper.getTypeFactory().constructParametricType(BulkGetResponse.class,
                TestDocument.class));
        assertEquals(2, response.getDocs().size(), "There are two documents in json above");
        assertEquals("a", response.getDocs().get(0).getId(), "Id was not properly deserialize");
        assertEquals("revA", response.getDocs().get(0).getRevision(), "Revision was not properly deserialize");
        assertEquals("valueA", response.getDocs().get(0).getValue(), "Value was not properly deserialize");
        assertEquals("value2a", response.getDocs().get(0).getValue2(), "Value2 was not properly deserialize");
        assertEquals("b", response.getDocs().get(1).getId(), "Id was not properly deserialize");
        assertEquals("revB", response.getDocs().get(1).getRevision(), "Revision was not properly deserialize");
        assertEquals("valueB", response.getDocs().get(1).getValue(), "Value was not properly deserialize");
        assertEquals("value2b", response.getDocs().get(1).getValue2(), "Value2 was not properly deserialize");
    }

}
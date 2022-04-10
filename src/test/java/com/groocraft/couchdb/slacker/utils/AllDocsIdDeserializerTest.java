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
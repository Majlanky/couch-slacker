/*
 * Copyright 2020 the original author or authors.
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
import com.groocraft.couchdb.slacker.EntityMetadata;
import com.groocraft.couchdb.slacker.TestDocument;
import com.groocraft.couchdb.slacker.ViewedDocument;
import com.groocraft.couchdb.slacker.structure.DocumentFindRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindContextSerializerTest {

    @Test
    void testViewed() throws JsonProcessingException {
        EntityMetadata metadata = new EntityMetadata(ViewedDocument.class);
        PartTree partTree = new PartTree("findByField", ViewedDocument.class);
        FindContext findContext = new FindContext(partTree, Collections.singletonMap("field", "testValue"), metadata);
        DocumentFindRequest request = new DocumentFindRequest(findContext, null, 100, null, Sort.unsorted(), false);
        String json = new ObjectMapper().writeValueAsString(request);
        assertEquals("{\"limit\":100,\"selector\":{\"$and\":[{\"type\":{\"$eq\":\"entity\"}},{\"$or\":[{\"field\":{\"$eq\":\"testValue\"}}]}]}}", json,
                "Serialized JSON is wrong. There must be condition about type inside and with the rest of serialized PartTree");
    }

    @Test
    void testNonViewed() throws JsonProcessingException {
        EntityMetadata metadata = new EntityMetadata(TestDocument.class);
        PartTree partTree = new PartTree("findByValue", TestDocument.class);
        FindContext findContext = new FindContext(partTree, Collections.singletonMap("value", "testValue"), metadata);
        DocumentFindRequest request = new DocumentFindRequest(findContext, null, 100, null, Sort.unsorted(), false);
        String json = new ObjectMapper().writeValueAsString(request);
        assertEquals("{\"limit\":100,\"selector\":{\"$or\":[{\"value\":{\"$eq\":\"testValue\"}}]}}", json, "Serialized JSON is wrong.");
    }

}
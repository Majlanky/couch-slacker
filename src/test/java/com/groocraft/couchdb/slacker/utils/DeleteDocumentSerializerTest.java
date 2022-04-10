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
import com.groocraft.couchdb.slacker.structure.BulkRequest;
import com.groocraft.couchdb.slacker.test.integration.TestDocument;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeleteDocumentSerializerTest {

    @Test
    void test() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(new DeleteDocumentSerializer<>(TestDocument.class));
        mapper.registerModule(module);
        BulkRequest<TestDocument> request = new BulkRequest<>(Collections.singletonList(new TestDocument("id", "rev", "value", "value2")));
        assertEquals("{\"docs\":[{\"_id\":\"id\",\"_rev\":\"rev\",\"value\":\"value\",\"value2\":\"value2\",\"value5\":false,\"_deleted\":true}]}",
                mapper.writeValueAsString(request), "Object should be serialized as is, only _deleted should be added");
    }

}
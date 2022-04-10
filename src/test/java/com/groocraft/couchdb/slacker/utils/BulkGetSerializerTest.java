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
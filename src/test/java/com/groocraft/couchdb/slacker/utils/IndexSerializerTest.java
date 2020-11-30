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
import com.groocraft.couchdb.slacker.structure.IndexCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexSerializerTest {

    @Test
    void test() throws JsonProcessingException {
        IndexCreateRequest request = new IndexCreateRequest("test", Collections.singletonList(Sort.Order.asc("value")));
        assertEquals("{\"index\":{\"fields\":[{\"value\":\"asc\"}]},\"type\":\"json\",\"name\":\"test\"}",
                new ObjectMapper().writeValueAsString(request), "Index request is not serialized properly");
    }

}
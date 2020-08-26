/*
 * Copyright 2014-2020 the original author or authors.
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

package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class IndexCreateRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("index")
    private final List<Map<String, String>> index;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("type")
    private final String type = "json";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("name")
    private final String name;

    public IndexCreateRequest(String name, Iterable<Sort.Order> fields) {
        this.name = name;
        this.index = new LinkedList<>();
        fields.forEach(o -> index.add(Map.of(o.getProperty(), o.getDirection().toString().toLowerCase())));
    }
}

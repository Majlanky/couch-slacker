/*
 * Copyright 2020-2022 the original author or authors.
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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Majlanky
 */
public class AllDocsIdDeserializer extends JsonDeserializer<List<String>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        List<String> data = new LinkedList<>();
        JsonNode root = p.getCodec().readTree(p);
        for(int i = 0; i < root.size(); i++){
            JsonNode object = root.get(i);
            if(object != null){
                data.add(object.get("id").textValue());
            }
        }
        return data;
    }
}

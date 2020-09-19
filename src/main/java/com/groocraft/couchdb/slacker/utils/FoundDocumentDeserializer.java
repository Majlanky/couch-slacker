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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.List;

/**
 * Implementation of {@link JsonSerializer} to ease deserialization of _find result. Because
 * {@link com.groocraft.couchdb.slacker.structure.DocumentFindResponse} is unified for all entities, documents wrapped inside has to be deserialize with
 * information of expected type ({@code DataT})
 *
 * @param <EntityT> type of entity (document)
 * @author Majlanky
 */
public class FoundDocumentDeserializer<EntityT> extends JsonDeserializer<List<EntityT>> {

    private final Class<EntityT> clazz;

    /**
     * @param clazz must not be {@literal null}
     */
    public FoundDocumentDeserializer(Class<EntityT> clazz) {
        Assert.notNull(clazz, "Clazz must not be null");
        this.clazz = clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntityT> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return new ObjectMapper().readValue(node.toString(), ctx.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}

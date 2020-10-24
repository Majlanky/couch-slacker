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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of {@link JsonSerializer} to ease serialization of Mango query from {@link FindContext}. Because of non-standard structure of Mango query it
 * is not easy to map it by objects. This implementation is not using redundant classes, and serialize {@link PartTree} "manually".
 *
 * @author Majlanky
 * @see JsonSerializer
 * @see Operation
 * @see FindContext
 */
public class FindContextSerializer extends JsonSerializer<FindContext> {

    private static final String OR = "$or";
    private static final String AND = "$and";

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(FindContext findContext, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        if (findContext.getEntityMetadata().isViewed()) {
            generator.writeStartObject();
            generator.writeArrayFieldStart(AND);
            write(generator, Operation.EQUALS, findContext.getEntityMetadata().getTypeField(), findContext.getEntityMetadata().getType());
        }

        serializePartTree(findContext.getPartTree(), findContext.getParameters(), generator);

        if (findContext.getEntityMetadata().isViewed()) {
            generator.writeEndArray();
            generator.writeEndObject();
        }
    }

    /**
     * Serialization of Spring {@link PartTree} which stands for condition tree of find request.
     *
     * @param partTree   must not be {@literal null}
     * @param parameters of conditions contained in {@code partTree}
     * @param generator  must not be {@literal null}
     * @throws IOException in case of exceptional state during creation of json
     */
    private void serializePartTree(PartTree partTree, Map<String, Object> parameters, JsonGenerator generator) throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart(OR);

        for (PartTree.OrPart orPart : partTree) {
            write(generator, orPart, parameters);
        }

        generator.writeEndArray();
        generator.writeEndObject();
    }

    /**
     * Serialization of {@link org.springframework.data.repository.query.parser.PartTree.OrPart}
     *
     * @param generator must not be {@literal null}
     * @param orPart    must not be {@literal null}
     * @throws IOException in case of exceptional state during creation of json
     */
    private void write(@NotNull JsonGenerator generator, @NotNull PartTree.OrPart orPart, @NotNull Map<String, Object> parameters) throws IOException {
        boolean isAnd = orPart.get().count() > 1;
        if (isAnd) {
            generator.writeStartObject();
            generator.writeArrayFieldStart(AND);
        }
        for (Part part : orPart) {
            write(generator, part, parameters);
        }
        if (isAnd) {
            generator.writeEndArray();
            generator.writeEndObject();
        }
    }

    /**
     * Serialization of {@link Part}
     *
     * @param generator must not be {@literal null}
     * @param part      must not be {@literal null}
     * @throws IOException in case of exceptional state during creation of json
     */
    private void write(@NotNull JsonGenerator generator, @NotNull Part part, @NotNull Map<String, Object> parameters) throws IOException {
        write(generator, Operation.of(part.getType()), part.getProperty().toDotPath(), parameters.get(part.getProperty().getLeafProperty().getSegment()));
    }

    /**
     * Serialization of operation with the given parameters.
     *
     * @param generator must not be {@literal null}
     * @param operation which should be serialized. Must not be {@literal null}
     * @param name      of attribute used in operation. Must not be {@literal null}
     * @param value     of operations attribute.
     * @throws IOException in case of exceptional state during creation of json
     */
    private void write(@NotNull JsonGenerator generator, @NotNull Operation operation, @NotNull String name, @Nullable Object value) throws IOException {
        generator.writeStartObject();
        generator.writeObjectFieldStart(name);
        operation.write(value, generator);
        generator.writeEndObject();
        generator.writeEndObject();
    }
}

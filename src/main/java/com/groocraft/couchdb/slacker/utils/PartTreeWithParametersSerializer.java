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
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.io.IOException;
import java.util.Map;

/**
 * Implementation of {@link JsonSerializer} to ease serialization of Mango query from {@link PartTreeWithParameters}. Because of non-standard structure of Mango query it
 * is not easy to map it by objects. This implementation is not using redundant classes, and serialize {@link PartTree} "manually".
 *
 * @author Majlanky
 * @see JsonSerializer
 * @see Operation
 * @see PartTreeWithParameters
 */
public class PartTreeWithParametersSerializer extends JsonSerializer<PartTreeWithParameters> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(PartTreeWithParameters partTree, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();
        generator.writeArrayFieldStart("$or");

        for (PartTree.OrPart orPart : partTree.getPartTree()) {
            write(generator, orPart, partTree.getParameters());
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
            generator.writeArrayFieldStart("$and");
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
        generator.writeStartObject();
        generator.writeObjectFieldStart(part.getProperty().getSegment());
        Operation.of(part.getType()).write(parameters.get(part.getProperty().getSegment()), generator);
        generator.writeEndObject();
        generator.writeEndObject();
    }
}

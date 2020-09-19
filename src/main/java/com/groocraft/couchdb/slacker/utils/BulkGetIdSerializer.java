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
import com.groocraft.couchdb.slacker.data.Reader;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @param <EntityT> Type of processed entity, from which id is read.
 * @author Majlanky
 */
public class BulkGetIdSerializer<EntityT> extends JsonSerializer<EntityT> {

    private final Class<EntityT> clazz;
    private final Reader<String> idReader;

    /**
     * @param clazz of entity. Must not be {@literal null}
     * @param idReader reader of entity for obtaining id. Must not be {@literal null}
     */
    public BulkGetIdSerializer(@NotNull Class<EntityT> clazz, @NotNull Reader<String> idReader) {
        Assert.notNull(clazz, "Clazz must not be null");
        Assert.notNull(idReader, "IdReader must not be null");
        this.clazz = clazz;
        this.idReader = idReader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<EntityT> handledType() {
        return clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(EntityT value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("id", idReader.read(value));
        gen.writeEndObject();
    }
}

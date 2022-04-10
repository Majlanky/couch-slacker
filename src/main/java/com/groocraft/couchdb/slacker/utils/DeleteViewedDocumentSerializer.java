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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * @param <EntityT> Type of entity which is using views (and type fields) and should be marked as deleted in a final request
 * @author Majlanky
 */
public class DeleteViewedDocumentSerializer<EntityT> extends WrappingSerializer<EntityT> {

    private final String typeField;
    private final String type;

    /**
     * @param clazz     of deleted entity. Must not be {@literal null}
     * @param typeField Name of field with stores a type of the entity. Must not be {@literal null} nor empty
     * @param type      of the entity. Must not be {@literal null} nor empty
     */
    public DeleteViewedDocumentSerializer(@NotNull Class<EntityT> clazz, @NotNull String typeField, @NotNull String type) {
        super(clazz);
        Assert.hasText(typeField, "TypeField must not be null nor empty");
        Assert.hasText(type, "Type must not be null nor empty");
        this.typeField = typeField;
        this.type = type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void serializedAdded(EntityT value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeObjectField("_deleted", true);
        gen.writeObjectField(typeField, type);
    }
}

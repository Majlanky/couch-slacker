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
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * There is more places where application needs to extends the serialized data by one ore more field. This is parent for all {@link JsonSerializer}s with the
 * feature.
 * @param <EntityT> type of entity which is processed
 * @author Majlanky
 */
public abstract class WrappingSerializer<EntityT> extends JsonSerializer<EntityT> {

    private final Class<EntityT> clazz;

    /**
     * @param clazz of wrapped entity. Must not be {@literal null}
     */
    protected WrappingSerializer(@NotNull Class<EntityT> clazz) {
        Assert.notNull(clazz, "Clazz must not be null");
        this.clazz = clazz;
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
    public void serialize(EntityT entity, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JavaType javaType = serializers.constructType(clazz);
        BeanDescription beanDesc = serializers.getConfig().introspect(javaType);
        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanOrAddOnSerializer(serializers,
                javaType,
                beanDesc,
                serializers.isEnabled(MapperFeature.USE_STATIC_TYPING));
        gen.writeStartObject();
        serializer.unwrappingSerializer(null).serialize(entity, gen, serializers);
        serializedAdded(entity, gen, serializers);
        gen.writeEndObject();
    }

    /**
     * Abstract method to add all extending data to the serialized data of an entity.
     * @param entity which is serialized. Must not be {@literal null}
     * @param gen {@link JsonGenerator}. Must not be {@literal null}
     * @param serializers {@link SerializerProvider}. Must not be {@literal null}
     * @throws IOException in case of problems during serialization
     */
    protected abstract void serializedAdded(EntityT entity, JsonGenerator gen, SerializerProvider serializers) throws IOException;
}

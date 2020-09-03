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
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;

import java.io.IOException;

public class DeleteDocumentSerializer<EntityT> extends JsonSerializer<EntityT> {

    private final Class<EntityT> clazz;

    public DeleteDocumentSerializer(Class<EntityT> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<EntityT> handledType() {
        return clazz;
    }

    @Override
    public void serialize(EntityT value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        JavaType javaType = serializers.constructType(clazz);
        BeanDescription beanDesc = serializers.getConfig().introspect(javaType);
        JsonSerializer<Object> serializer = BeanSerializerFactory.instance.findBeanOrAddOnSerializer(serializers,
                javaType,
                beanDesc,
                serializers.isEnabled(MapperFeature.USE_STATIC_TYPING));
        gen.writeStartObject();
        serializer.unwrappingSerializer(null).serialize(value, gen, serializers);
        gen.writeObjectField("_deleted", true);
        gen.writeEndObject();
    }
}

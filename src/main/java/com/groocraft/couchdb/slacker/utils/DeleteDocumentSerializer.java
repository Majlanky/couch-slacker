package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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

package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.groocraft.couchdb.slacker.data.Reader;

import java.io.IOException;

public class BulkGetIdSerializer<EntityT> extends JsonSerializer<EntityT> {

    private final Class<EntityT> clazz;
    private final Reader<String> idReader;

    public BulkGetIdSerializer(Class<EntityT> clazz, Reader<String> idReader) {
        this.clazz = clazz;
        this.idReader = idReader;
    }

    @Override
    public Class<EntityT> handledType() {
        return clazz;
    }

    @Override
    public void serialize(EntityT value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("id", idReader.read(value));
        gen.writeEndObject();
    }
}

package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.groocraft.couchdb.slacker.annotation.Index;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;

import java.io.IOException;
import java.util.Optional;

/**
 * Implementation of {@link JsonSerializer} to ease serialization of Mango query from {@link PartTree}. Because of non-standard structure of Mango query it
 * is not easy to map it by objects. This implementation is not using redundant classes, and serialize {@link PartTree} "manually".
 *
 * @author Majlanky
 * @see JsonSerializer
 */
public class PartTreeSerializer extends JsonSerializer<PartTree> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(PartTree partTree, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeStartObject();
        //generator.writeObjectFieldStart("selector");
        generator.writeArrayFieldStart("$or");

        for (PartTree.OrPart orPart : partTree) {
            write(generator, orPart);
        }

        generator.writeEndArray();
        generator.writeEndObject();
        //generator.writeEndObject();
    }

    /**
     * Serialization of {@link org.springframework.data.repository.query.parser.PartTree.OrPart}
     *
     * @param generator must not be {@literal null}
     * @param orPart    must not be {@literal null}
     * @throws IOException in case of exceptional state during creation of json
     */
    private void write(JsonGenerator generator, PartTree.OrPart orPart) throws IOException {
        boolean isAnd = orPart.get().count() > 1;
        if (isAnd) {
            generator.writeStartObject();
            generator.writeArrayFieldStart("$and");
        }
        for (Part part : orPart) {
            write(generator, part);
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
    private void write(JsonGenerator generator, Part part) throws IOException {
        //TODO rework to be able process more complex expressions
        generator.writeStartObject();
        generator.writeObjectFieldStart(part.getProperty().getSegment());
        generator.writeObjectField(Operation.get(part.getType()), ":" + part.getProperty().getSegment());
        generator.writeEndObject();
        generator.writeEndObject();
    }
}

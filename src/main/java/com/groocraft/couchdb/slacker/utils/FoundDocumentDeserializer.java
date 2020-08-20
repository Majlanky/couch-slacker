package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    public FoundDocumentDeserializer(Class<EntityT> clazz) {
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

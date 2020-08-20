package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("unused")
public class AllDocsIdDeserializer extends JsonDeserializer<List<String>> {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        List<String> data = new LinkedList<>();
        JsonNode root = p.getCodec().readTree(p);
        for(int i = 0; i < root.size(); i++){
            JsonNode object = root.get(i);
            if(object != null){
                data.add(object.get("id").textValue());
            }
        }
        return data;
    }
}

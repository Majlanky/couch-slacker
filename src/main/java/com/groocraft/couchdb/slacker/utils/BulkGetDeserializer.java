package com.groocraft.couchdb.slacker.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BulkGetDeserializer<EntityT> extends JsonDeserializer<List<EntityT>> {

    private Class<EntityT> clazz;

    public BulkGetDeserializer(Class<EntityT> clazz) {
        this.clazz = clazz;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EntityT> deserialize(JsonParser p, DeserializationContext ctx) throws IOException, JsonProcessingException {
        List<EntityT> data = new LinkedList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = p.getCodec().readTree(p);
        for(int i = 0; i < root.size(); i++){
            JsonNode object = root.get(i).get("docs").get(0).get("ok");
            if(object != null){
                data.add(mapper.readValue(object.toString(), clazz));
            }
        }
        return data;
    }
}

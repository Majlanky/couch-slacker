package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class IndexCreateRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("index")
    private final List<Map<String, String>> index;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("type")
    private final String type = "json";

    public IndexCreateRequest(String name, Iterable<Sort.Order> fields) {
        this.index = new LinkedList<>();
        fields.forEach(o -> index.add(Map.of(o.getProperty(), o.getDirection().toString().toLowerCase())));
    }
}

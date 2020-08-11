package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class IndexCreateRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("index")
    private List<Map<String, String>> index;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("name")
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("type")
    private String type = "json";

    public IndexCreateRequest(String name, Iterable<Sort.Order> fields) {
        this.name = name;
        this.index = new LinkedList<>();
        fields.forEach(o -> index.add(Map.of(o.getProperty(), o.getDirection().toString().toLowerCase())));
    }
}

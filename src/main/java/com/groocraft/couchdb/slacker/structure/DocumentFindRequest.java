package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.groocraft.couchdb.slacker.annotation.Index;
import com.groocraft.couchdb.slacker.utils.PartTreeSerializer;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.PartTree;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DocumentFindRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("use_index")
    private String[] useIndex;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("limit")
    private Integer limit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sort")
    private List<Map<String, String>> sort;

    @JsonProperty("selector")
    @JsonSerialize(using = PartTreeSerializer.class)
    private PartTree partTree;

    /**
     * @param partTree
     * @param index
     * @param sortParameter
     */
    public DocumentFindRequest(PartTree partTree, Optional<Index> index, Sort sortParameter) {
        //TODO paging?
        //TODO what about filtering by sub-attributes? findByAddressZipCode where zipcode is part of address structure inside the entity?
        this.partTree = partTree;
        index.ifPresent(i -> useIndex = i.value());
        Optional.ofNullable(partTree.getMaxResults()).ifPresent(i -> limit = i);
        if (partTree.getSort().isSorted() || sortParameter.isSorted()) {
            sort = new LinkedList<>();
            sortParameter.and(partTree.getSort()).forEach(o -> sort.add(Map.of(o.getProperty(), o.getDirection().toString().toLowerCase())));
        }
    }

}

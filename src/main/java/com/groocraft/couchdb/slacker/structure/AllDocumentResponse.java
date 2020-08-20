package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.groocraft.couchdb.slacker.utils.AllDocsIdDeserializer;

import java.util.List;

@SuppressWarnings("unused")
public class AllDocumentResponse {

    private int offset;

    @JsonDeserialize(using = AllDocsIdDeserializer.class)
    private List<String> rows;

    @JsonProperty("total_rows")
    private int totalRows;

    public int getOffset() {
        return offset;
    }

    public List<String> getRows() {
        return rows;
    }

    public int getTotalRows() {
        return totalRows;
    }
}

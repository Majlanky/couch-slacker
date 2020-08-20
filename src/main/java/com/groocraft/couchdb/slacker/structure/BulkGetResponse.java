package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@SuppressWarnings("unused")
public class BulkGetResponse<EntityT> {

    @JsonProperty("results")
    List<EntityT> docs;

    public List<EntityT> getDocs() {
        return docs;
    }

    public void setDocs(List<EntityT> docs) {
        this.docs = docs;
    }
}

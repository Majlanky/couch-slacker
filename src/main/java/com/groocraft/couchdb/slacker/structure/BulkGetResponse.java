package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.swing.text.html.parser.Entity;
import java.util.List;
import java.util.Set;

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

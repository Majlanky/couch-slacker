package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Document;

@Document
public class NoRevisionTestDocument {

    @JsonProperty("_id")
    String id;

}

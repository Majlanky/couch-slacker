package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Document;

@Document(database = "test")
public class FieldTestDocument {

    @JsonProperty("_id")
    String a;

    @JsonProperty("_rev")
    String b;

}

package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Database;

@Database("test")
public class FieldTestDocument {

    @JsonProperty("_id")
    String a;

    @JsonProperty("_rev")
    String b;

}

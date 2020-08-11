package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoDatabaseTestDocument {

    @JsonProperty("_id")
    String a;

    @JsonProperty("_rev")
    String b;

}

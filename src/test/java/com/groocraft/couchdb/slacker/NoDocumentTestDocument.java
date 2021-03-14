package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NoDocumentTestDocument {

    @JsonProperty("_id")
    String a;

    @JsonProperty("_rev")
    String b;

}

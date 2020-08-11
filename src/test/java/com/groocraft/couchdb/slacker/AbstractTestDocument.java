package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbstractTestDocument {

    @JsonProperty("_rev")
    String b;

    boolean bCalled;

    public void setB(String b){
        bCalled = true;
        this.b = b;
    }

}

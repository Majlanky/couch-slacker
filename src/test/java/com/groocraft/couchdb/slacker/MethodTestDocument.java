package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Database;

@Database("test")
public class MethodTestDocument {

    String a;

    String b;

    boolean aCalled;

    boolean bCalled;

    @JsonProperty("_id")
    public String getA() {
        aCalled = true;
        return a;
    }

    @JsonProperty("_id")
    public void setA(String a) {
        aCalled = true;
        this.a = a;
    }

    @JsonProperty("_rev")
    public String getB() {
        bCalled = true;
        return b;
    }

    @JsonProperty("_rev")
    public void setB(String b) {
        bCalled = true;
        this.b = b;
    }
}

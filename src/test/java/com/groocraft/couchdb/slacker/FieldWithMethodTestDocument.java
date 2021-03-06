package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Document;

@Document("test")
public class FieldWithMethodTestDocument {

    @JsonProperty("_id")
    String a;

    @JsonProperty("_rev")
    String b;

    boolean aCalled;

    boolean bCalled;

    public String getA() {
        aCalled = true;
        return a;
    }

    public void setA(String a) {
        aCalled = true;
        this.a = a;
    }

    public String getB() {
        bCalled = true;
        return b;
    }

    public void setB(String b) {
        bCalled = true;
        this.b = b;
    }
}

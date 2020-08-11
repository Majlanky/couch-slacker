package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Database;

@Database("test")
public class TestDocument extends Document {

    @JsonProperty("value")
    String value;

    @JsonProperty("value2")
    String value2;

    public TestDocument() {
    }

    public TestDocument(String value) {
        this.value = value;
    }

    public TestDocument(String value, String value2){
        this.value = value;
        this.value2 = value2;
    }

    public TestDocument(String id, String revision, String value) {
        super(id, revision);
        this.value = value;
    }

    public TestDocument(String id, String revision, String value, String value2) {
        this(id, revision, value);
        this.value2 = value2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }


}

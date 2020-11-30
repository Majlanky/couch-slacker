package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Document;

import java.util.List;

@Document("test")
public class TestDocument extends DocumentBase {

    @JsonProperty("value")
    String value;

    @JsonProperty("value2")
    String value2;

    @JsonProperty("value3")
    Integer value3;

    @JsonProperty("value4")
    List<String> value4;

    @JsonProperty("value5")
    boolean value5;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("address")
    TestDocumentAddress address;

    public TestDocument() {
    }

    public TestDocument(TestDocumentAddress address) {
        this.address = address;
    }

    public TestDocument(String value) {
        this.value = value;
    }

    public TestDocument(String value, String value2) {
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

    public TestDocument(int value3) {
        this.value3 = value3;
    }

    public TestDocument(List<String> value4) {
        this.value4 = value4;
    }

    public TestDocument(boolean value5) {
        this.value5 = value5;
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

    public Integer getValue3() {
        return value3;
    }

    public void setValue3(Integer value3) {
        this.value3 = value3;
    }

    public List<String> getValue4() {
        return value4;
    }

    public void setValue4(List<String> value4) {
        this.value4 = value4;
    }

    public boolean isValue5() {
        return value5;
    }

    public void setValue5(boolean value5) {
        this.value5 = value5;
    }

    public TestDocumentAddress getAddress() {
        return address;
    }

    public void setAddress(TestDocumentAddress address) {
        this.address = address;
    }


}

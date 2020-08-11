package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Database;

@Database("test")
public class InheritedTestDocument extends AbstractTestDocument{

    String a;

    boolean aCalled;

    @JsonProperty("_id")
    public String getA(){
        aCalled = true;
        return a;
    }

    @JsonProperty("_id")
    public void setA(String a){
        aCalled = true;
        this.a = a;
    }

}

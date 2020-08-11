package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.groocraft.couchdb.slacker.annotation.Database;

@Database("[/]")
public class ErrorTestDocument extends Document{

    @JsonProperty("value")
    String value;

}

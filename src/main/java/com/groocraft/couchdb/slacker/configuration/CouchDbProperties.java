package com.groocraft.couchdb.slacker.configuration;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@ConfigurationProperties(prefix = "couchdb.client")
@AllArgsConstructor
@NoArgsConstructor
public class CouchDbProperties {

    public static final String COUCH_ID_NAME = "_id";
    public static final String COUCH_REVISION_NAME = "_rev";

    private String url;

    private String username;

    private String password = "";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

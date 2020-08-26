/*
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groocraft.couchdb.slacker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties pojo class for Couch Slacker configuration. It is used in {@link CouchSlackerConfiguration} class
 *
 * @author Majlanky
 * @see CouchSlackerConfiguration
 */
@ConfigurationProperties(prefix = "couchdb.client")
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

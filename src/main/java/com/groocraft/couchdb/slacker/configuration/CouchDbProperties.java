/*
 * Copyright 2020 the original author or authors.
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

import org.hibernate.validator.constraints.URL;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

/**
 * Properties pojo class for Couch Slacker configuration. It is used in {@link CouchSlackerConfiguration} class
 *
 * @author Majlanky
 * @see CouchSlackerConfiguration
 */
@Validated
@ConfigurationProperties(prefix = "couchdb.client")
public class CouchDbProperties {

    public static final String COUCH_ID_NAME = "_id";
    public static final String COUCH_REVISION_NAME = "_rev";

    /**
     * Must be valid URL.
     * Port is mandatory.
     * Scheme can be http or https.
     * Scheme is not mandatory, http is used if not present.
     */
    @URL
    private String url;

    /**
     * Name of user used for authentication to CouchDB.
     * Must not be empty.
     */
    @NotEmpty
    private String username;

    /**
     * Password for name used for authentication to CouchDB.
     * Must not be empty.
     */
    @NotEmpty
    private String password;

    /**
     * If bulk operation (findAll, deleteAll, etc.) is executed, this is the limit of document number processed in one batch.
     * Operation requesting operation with more documents than limit is processed in more batches.
     * Minimum is 10, maximum is 100000.
     * Default value is 10000.
     */
    @Min(10)
    @Max(100000)
    private int bulkMaxSize = 10000;

    /**
     * Flag which can turn on/off execution stats in the _find query result.
     * The stats are logged if turned on.
     * Default value is false.
     */
    private boolean findExecutionStats = false;

    public CouchDbProperties() {
    }

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

    public int getBulkMaxSize() {
        return bulkMaxSize;
    }

    public void setBulkMaxSize(int bulkMaxSize) {
        this.bulkMaxSize = bulkMaxSize;
    }

    public boolean isFindExecutionStats() {
        return findExecutionStats;
    }

    public void setFindExecutionStats(boolean findExecutionStats) {
        this.findExecutionStats = findExecutionStats;
    }

    public void copy(CouchDbProperties properties) {
        setPassword(properties.getPassword());
        setUsername(properties.getUsername());
        setUrl(properties.url);
        setBulkMaxSize(properties.getBulkMaxSize());
        setFindExecutionStats(properties.isFindExecutionStats());
    }
}

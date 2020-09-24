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

package com.groocraft.couchdb.slacker.structure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

/**
 * Pojo class to ease reading responses to database _find request.
 *
 * @param <EntityT> Type of result entity
 * @author Majlanky
 */
public class DocumentFindResponse<EntityT> {

    @JsonProperty("docs")
    private List<EntityT> documents;

    @JsonProperty("execution_stats")
    private ExecutionStats executionStats;

    @JsonProperty("error")
    private String error;

    @JsonProperty("bookmark")
    private String bookmark;

    @JsonProperty("warning")
    private String warning;

    @JsonProperty("reason")
    private String reason;

    public List<EntityT> getDocuments() {
        return documents;
    }

    public void setDocuments(List<EntityT> documents) {
        this.documents = documents;
    }

    public Optional<ExecutionStats> getExecutionStats() {
        return Optional.ofNullable(executionStats);
    }

    public void setExecutionStats(ExecutionStats executionStats) {
        this.executionStats = executionStats;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(String bookmark) {
        this.bookmark = bookmark;
    }

    public Optional<String> getWarning() {
        return Optional.ofNullable(warning);
    }

    public void setWarning(String warning) {
        this.warning = warning;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}

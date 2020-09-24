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

/**
 * Pojo class to ease reading part of {@link DocumentFindResponse}.
 *
 * @author Majlanky
 * @see DocumentFindResponse
 */
public class ExecutionStats {

    @JsonProperty("total_keys_examined")
    private int totalKeysExamined;

    @JsonProperty("total_docs_examined")
    private int totalDocsExamined;

    @JsonProperty("total_quorum_docs_examined")
    private int totalQuorumDocsExamined;

    @JsonProperty("results_returned")
    private int resultsReturned;

    @JsonProperty("execution_time_ms")
    private int executionTimeMs;

    public int getTotalKeysExamined() {
        return totalKeysExamined;
    }

    public void setTotalKeysExamined(int totalKeysExamined) {
        this.totalKeysExamined = totalKeysExamined;
    }

    public int getTotalDocsExamined() {
        return totalDocsExamined;
    }

    public void setTotalDocsExamined(int totalDocsExamined) {
        this.totalDocsExamined = totalDocsExamined;
    }

    public int getTotalQuorumDocsExamined() {
        return totalQuorumDocsExamined;
    }

    public void setTotalQuorumDocsExamined(int totalQuorumDocsExamined) {
        this.totalQuorumDocsExamined = totalQuorumDocsExamined;
    }

    public int getResultsReturned() {
        return resultsReturned;
    }

    public void setResultsReturned(int resultsReturned) {
        this.resultsReturned = resultsReturned;
    }

    public int getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(int executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public String toString() {
        return "ExecutionStats{" +
                "totalKeysExamined=" + totalKeysExamined +
                ", totalDocsExamined=" + totalDocsExamined +
                ", totalQuorumDocsExamined=" + totalQuorumDocsExamined +
                ", resultsReturned=" + resultsReturned +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}

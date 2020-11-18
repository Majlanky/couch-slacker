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

package com.groocraft.couchdb.slacker;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class FindRequestBase implements FindRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("use_index")
    private String[] useIndex;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("limit")
    private Integer limit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("skip")
    private Long skip;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sort")
    private List<Map<String, String>> sortFields;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("execution_stats")
    private Boolean addExecutionStatsToResult;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("bookmark")
    private String bookmark;

    @JsonIgnore
    private final Sort sort;

    /**
     * @param skip                      number of document which should be skipped. Can be {@literal null} which means do not skip
     * @param limit                     of document number in the result
     * @param index                     Array of indexes, which should be used for querying. Can be {@literal null} which means do not use any index
     * @param sort                      All order rules. Can be {@literal null} which means do not sort.
     * @param addExecutionStatsToResult setting if the result should container information about execution of Mango query
     */
    public FindRequestBase(@Nullable Long skip, @Nullable Integer limit,
                           @Nullable String[] index, @NotNull Sort sort, boolean addExecutionStatsToResult) {
        Assert.isTrue(limit == null || limit > 0, "Limit must be positive number or null");
        if (index != null) {
            useIndex = index;
        }
        this.limit = limit;
        if (skip != null) {
            this.skip = skip;
        }
        this.sort = sort;
        if (sort.isSorted()) {
            sortFields = new LinkedList<>();
            sort.forEach(o -> sortFields.add(Collections.singletonMap(o.getProperty(), o.getDirection().toString().toLowerCase())));
        }
        if (addExecutionStatsToResult) {
            this.addExecutionStatsToResult = true;
        }
    }

    @Override
    public void setLimit(@Nullable Integer limit) {
        this.limit = limit;
    }

    @Override
    public @Nullable Integer getLimit() {
        return limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBookmark(@Nullable String bookmark) {
        this.bookmark = bookmark;
    }

    public @NotNull Sort getSort() {
        return sort;
    }

    public void setSkip(@Nullable Long skip) {
        this.skip = skip;
    }

    public @Nullable Long getSkip() {
        return skip;
    }
}

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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.groocraft.couchdb.slacker.utils.FindContext;
import com.groocraft.couchdb.slacker.utils.FindContextSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Pojo class for creating json Mango find query.
 *
 * @author Majlanky
 */
@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
public class DocumentFindRequest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("use_index")
    private String[] useIndex;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("limit")
    private final Integer limit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("skip")
    private Long skip;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("sort")
    private List<Map<String, String>> sort;

    @JsonProperty("selector")
    @JsonSerialize(using = FindContextSerializer.class)
    private final FindContext findContext;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("execution_stats")
    private Boolean addExecutionStatsToResult;

    /**
     * @param findContext    with all necessary information about finds selector. Must not be {@literal null}
     * @param skip                      number of document which should be skipped. Can be {@literal null} which means do not skip
     * @param limit                     of document number in the result
     * @param index                     Array of indexes, which should be used for querying. Can be {@literal null} which means do not use any index
     * @param order                     List of all order rules. Can be {@literal null} which means do not order.
     * @param addExecutionStatsToResult setting if the result should container information about execution of Mango query
     */
    public DocumentFindRequest(@NotNull FindContext findContext, @Nullable Long skip, int limit,
                               @Nullable String[] index, @Nullable List<Sort.Order> order, boolean addExecutionStatsToResult) {
        Assert.notNull(findContext, "FindContext must not be null.");
        Assert.isTrue(limit > 0, "Limit must be positive number");
        this.findContext = findContext;
        if (index != null) {
            useIndex = index;
        }
        this.limit = limit;
        if (skip != null) {
            this.skip = skip;
        }
        if (order != null) {
            sort = new LinkedList<>();
            order.forEach(o -> sort.add(Map.of(o.getProperty(), o.getDirection().toString().toLowerCase())));
        }
        if (addExecutionStatsToResult) {
            this.addExecutionStatsToResult = true;
        }
    }

    /**
     * Method which is setting skip parameter of Mango query to get next bunch of documents with of the same query. How much documents is obtained and how
     * much is skipped is based on the given parameter
     *
     * @param count how much to skip
     */
    public void skipNext(int count) {
        skip = skip == null ? count : skip + count;
    }

}

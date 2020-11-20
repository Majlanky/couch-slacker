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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Map;

public class TestFindRequest extends FindRequestBase {

    @JsonProperty("selector")
    private final Map<String, String> selector = Collections.emptyMap();

    /**
     * @param skip                      number of document which should be skipped. Can be {@literal null} which means do not skip
     * @param limit                     of document number in the result
     * @param index                     Array of indexes, which should be used for querying. Can be {@literal null} which means do not use any index
     * @param sort                      All order rules. Can be {@literal null} which means do not sort.
     * @param addExecutionStatsToResult setting if the result should container information about execution of Mango query
     */
    public TestFindRequest(@Nullable Long skip, @Nullable Integer limit, @Nullable String[] index, @NotNull Sort sort, boolean addExecutionStatsToResult) {
        super(skip, limit, index, sort, addExecutionStatsToResult);
    }

    public Object getSelector() {
        return selector;
    }

    @Override
    @JsonIgnore
    public @NotNull String getJavaScriptCondition(@NotNull ObjectMapper objectMapper) {
        throw new IllegalStateException("TestFind do not provide javascript conversion");
    }
}

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;

public interface FindRequest {

    /**
     * Method which is setting bookmark parameter of Mango query to get next bunch of documents of the same query. How much documents is obtained and how
     * much is "skipped" is based on the given parameter
     *
     * @param bookmark how much to skip
     */
    void setBookmark(@Nullable String bookmark);

    /**
     * Method used to optimize query (chunking of big bulk requests)
     *
     * @param limit null if request must be unlimited, number in other cases
     */
    void setLimit(@Nullable Integer limit);

    /**
     * @return limit of the query. Null means no limit is wanted
     */
    @Nullable Integer getLimit();

    /**
     * Method used mostly as remover of skip from the request during internal process. Must be considered, or query strategy can fail.
     *
     * @param skip number of skipped documents. Null means no skip at all.
     */
    void setSkip(@Nullable Long skip);

    /**
     * @return how many documents should be skipped. Null means no skip at all.
     */
    @Nullable Long getSkip();

    /**
     * @return Sort information about the request
     */
    @NotNull Sort getSort();

    /**
     * @return Null or requested strategy for querying. Null causing usage of the default strategy.
     */
    @Nullable QueryStrategy getQueryStrategy();

    /**
     * Method returns javascript condition matching the request. Condition is used during {@link QueryStrategy#VIEW} strategy as part of condition in map
     * function of the view.
     *
     * @param objectMapper must not be {@literal null}
     * @return javascript condition matching the request
     * @throws JsonProcessingException in case one of {@link com.groocraft.couchdb.slacker.utils.Operation} is non-serializable.
     */
    @NotNull String getJavaScriptCondition(@NotNull ObjectMapper objectMapper) throws JsonProcessingException;
}

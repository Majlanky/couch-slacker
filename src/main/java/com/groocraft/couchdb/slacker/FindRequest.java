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

    void setLimit(@Nullable Integer limit);

    @Nullable Integer getLimit();

    void setSkip(@Nullable Long skip);

    @Nullable Long getSkip();

    @NotNull Sort getSort();

    @NotNull String getJavaScriptCondition();
}

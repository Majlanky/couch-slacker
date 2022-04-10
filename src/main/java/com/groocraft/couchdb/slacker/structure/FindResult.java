/*
 * Copyright 2020-2022 the original author or authors.
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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Class for wrapping all information about find done by margo or view way. It contains all found results and bookmarks if provided (bookmarks are provided
 * only when find is done thru _find endpoint. Views do not provide bookmarks.)
 *
 * @param <EntityT> type of entities inside of the result
 * @author Majlanky
 */
public class FindResult<EntityT> {

    private final List<EntityT> entities;
    private final Map<Integer, String> bookmarks;

    /**
     * @param entities  must not be {@literal null}
     * @param bookmarks must not be {@literal null}
     */
    private FindResult(@NotNull List<EntityT> entities, @NotNull Map<Integer, String> bookmarks) {
        this.entities = entities;
        this.bookmarks = bookmarks;
    }

    /**
     * Default way to create instance of {@link FindResult}.
     *
     * @param entities  must not be {@literal null}
     * @param bookmarks must not be {@literal null}
     * @param <EntityT> type of entities returned in the result
     * @return {@literal non-null} instance
     */
    public static <EntityT> @NotNull FindResult<EntityT> of(@NotNull List<EntityT> entities, @NotNull Map<Integer, String> bookmarks) {
        return new FindResult<>(entities, bookmarks);
    }

    public @NotNull List<EntityT> getEntities() {
        return entities;
    }

    public @NotNull Map<Integer, String> getBookmarks() {
        return bookmarks;
    }

}

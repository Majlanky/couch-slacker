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

/**
 * Interface for all generators used for new IDs.
 * @param <EntityT> type of new entities
 *
 * @author Majlanky
 */
public interface IdGenerator<EntityT> {

    /**
     * Method to obtain a new ID.
     * @param e new entity for which id should be generated. Must not be {@literal null}
     * @return new ID. Can not be {@literal null}
     */
    @NotNull String generate(@NotNull EntityT e);

    /**
     * @return {@link Class} for which instances ID can be generated.
     */
    @NotNull Class<EntityT> getEntityClass();

}

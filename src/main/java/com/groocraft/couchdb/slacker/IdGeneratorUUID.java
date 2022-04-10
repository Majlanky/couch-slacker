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

package com.groocraft.couchdb.slacker;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Implementation of {@link IdGenerator} using {@link UUID#randomUUID()} as new ID source.
 *
 * @author Majlanky
 */
public class IdGeneratorUUID implements IdGenerator<Object> {

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull String generate(@NotNull Object e) {
        Assert.notNull(e, "Object must not be null");
        return UUID.randomUUID().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public @NotNull Class<Object> getEntityClass() {
        return Object.class;
    }
}

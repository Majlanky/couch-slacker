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

package com.groocraft.couchdb.slacker.utils;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.util.Map;


/**
 * Class to wrapping {@link PartTree} which bares semantic with {@link Map} of named parameters. Together it provides whole information of wanted query. It
 * is used for better pass ability to {@link PartTreeWithParametersSerializer}
 *
 * @author Majlanky
 * @see PartTree
 * @see PartTreeWithParametersSerializer
 */
public class PartTreeWithParameters {

    private final PartTree partTree;
    private final Map<String, Object> parameters;

    /**
     * @param partTree   must not be {@literal null}
     * @param parameters must not be {@literal null}
     */
    public PartTreeWithParameters(@NotNull PartTree partTree, @NotNull Map<String, Object> parameters) {
        Assert.notNull(partTree, "PartTree must not be null");
        Assert.notNull(parameters, "Parameters must not be null");
        this.partTree = partTree;
        this.parameters = parameters;
    }

    public @NotNull Map<String, Object> getParameters() {
        return parameters;
    }

    public @NotNull PartTree getPartTree() {
        return partTree;
    }
}

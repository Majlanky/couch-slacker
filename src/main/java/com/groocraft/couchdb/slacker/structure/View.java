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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

/**
 * Structure representing CouchDB view metadata.
 *
 * @author Majlanky
 */
public class View {

    @JsonIgnore
    private String name;

    @JsonProperty("map")
    private String mapFunction;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("reduce")
    private String reduceFunction;

    /**
     * @param name           of the new view. Must not be {@literal null}
     * @param mapFunction    used for indexing the view. Must not be {@literal null}
     * @param reduceFunction used for reducing data in the view
     */
    public View(@NotNull String name, @NotNull String mapFunction, @Nullable String reduceFunction) {
        Assert.notNull(name, "Name must not be null.");
        Assert.notNull(mapFunction, "MapFunction must not be null");
        this.name = name;
        this.mapFunction = mapFunction;
        this.reduceFunction = reduceFunction;
    }

    /**
     * Constructor for Jackson deserialization.
     */
    public View() {
    }

    public String getName() {
        return name;
    }

    public String getMapFunction() {
        return mapFunction;
    }

    public void setMapFunction(String mapFunction) {
        this.mapFunction = mapFunction;
    }

    public String getReduceFunction() {
        return reduceFunction;
    }

    public void setReduceFunction(String reduceFunction) {
        this.reduceFunction = reduceFunction;
    }
}

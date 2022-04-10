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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.groocraft.couchdb.slacker.DocumentBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Class for serialization of design documents of CouchDB.
 *
 * @author Majlanky
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DesignDocument extends DocumentBase {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, View> views;

    /**
     * This constructor is for new instance from DB point of view. It is caused by naming convention of design documents in CouchDB. To make names compatible
     * with CouchDB, there is the need to add prefix _design/ before name. When a design document is read from DB the prefix is already there.
     *
     * @param name  of the new design document. Must not be {@literal null}
     * @param views of the new design document. Must not be {@literal null}
     */
    public DesignDocument(@Nullable String name, @NotNull Set<View> views) {
        super("_design/" + name, null);
        Assert.notNull(views, "Views must not be null");
        this.views = new HashMap<>();
        views.forEach(this::addView);
    }

    /**
     * Constructor for Jackson deserialization.
     */
    public DesignDocument() {
    }

    public @NotNull Map<String, View> getViews() {
        return views;
    }

    /**
     * @param view to add. Must not be {@literal null}
     */
    public void addView(@NotNull View view) {
        views.put(view.getName(), view);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DesignDocument that = (DesignDocument) o;
        return views.equals(that.views);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), views);
    }
}

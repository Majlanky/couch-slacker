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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.groocraft.couchdb.slacker.utils.AllDocsIdDeserializer;

import java.util.List;

@SuppressWarnings("unused")
public class AllDocumentResponse {

    private int offset;

    @JsonDeserialize(using = AllDocsIdDeserializer.class)
    private List<String> rows;

    @JsonProperty("total_rows")
    private int totalRows;

    public int getOffset() {
        return offset;
    }

    public List<String> getRows() {
        return rows;
    }

    public int getTotalRows() {
        return totalRows;
    }
}

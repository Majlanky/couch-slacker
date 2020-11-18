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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.groocraft.couchdb.slacker.FindRequestBase;
import com.groocraft.couchdb.slacker.utils.FindContext;
import com.groocraft.couchdb.slacker.utils.FindContextSerializer;
import com.groocraft.couchdb.slacker.utils.Operation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.data.repository.query.parser.PartTree;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * Pojo class for creating json Mango find query.
 *
 * @author Majlanky
 */
public class DocumentFindRequest extends FindRequestBase {

    @JsonProperty("selector")
    @JsonSerialize(using = FindContextSerializer.class)
    private final FindContext findContext;

    /**
     * @param findContext               with all necessary information about finds selector. Must not be {@literal null}
     * @param skip                      number of document which should be skipped. Can be {@literal null} which means do not skip
     * @param limit                     of document number in the result
     * @param index                     Array of indexes, which should be used for querying. Can be {@literal null} which means do not use any index
     * @param sort                      All order rules. Can be {@literal null} which means do not sort.
     * @param addExecutionStatsToResult setting if the result should container information about execution of Mango query
     */
    public DocumentFindRequest(@NotNull FindContext findContext, @Nullable Long skip, @Nullable Integer limit,
                               @Nullable String[] index, @NotNull Sort sort, boolean addExecutionStatsToResult) {
        super(skip, limit, index, sort, addExecutionStatsToResult);
        Assert.notNull(findContext, "FindContext must not be null.");
        this.findContext = findContext;
    }

    @JsonIgnore
    @Override
    public @NotNull String getJavaScriptCondition() {
        StringBuilder builder = new StringBuilder();
        if (findContext.getEntityMetadata().isViewed()) {
            builder.append("(");
            builder.append(Operation.EQUALS.jsCondition(findContext.getEntityMetadata().getTypeField(), findContext.getEntityMetadata().getType()));
            builder.append(" && ");
        }

        getJavaScriptCondition(findContext.getPartTree(), findContext.getParameters(), builder);

        if (findContext.getEntityMetadata().isViewed()) {
            builder.append(")");
        }
        return builder.toString();
    }

    private void getJavaScriptCondition(PartTree partTree, Map<String, Object> parameters, StringBuilder builder) {
        builder.append("(");

        for (PartTree.OrPart orPart : partTree) {
            write(builder, orPart, parameters);
            builder.append(" || ");
        }
        builder.delete(builder.length() - 4, builder.length());

        builder.append(")");
    }

    private void write(@NotNull StringBuilder builder, @NotNull PartTree.OrPart orPart, @NotNull Map<String, Object> parameters) {
        boolean isAnd = orPart.get().count() > 1;
        if (isAnd) {
            builder.append("(");
        }
        for (Part part : orPart) {
            write(builder, part, parameters);
            if (isAnd) {
                builder.append(" && ");
            }
        }
        if (isAnd) {
            builder.delete(builder.length() - 4, builder.length());
            builder.append(")");
        }
    }

    private void write(@NotNull StringBuilder builder, @NotNull Part part, @NotNull Map<String, Object> parameters) {
        builder.append(Operation.of(part.getType()).jsCondition(part.getProperty().toDotPath(),
                parameters.get(part.getProperty().getLeafProperty().getSegment())));
    }

}

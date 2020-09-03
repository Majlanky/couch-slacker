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

import com.fasterxml.jackson.core.JsonGenerator;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.query.parser.Part;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Enum to map {@link Part.Type} operation to CouchDB Mango query operators.
 *
 * @author Majlanky
 */
public enum Operation {

    EQUALS(Part.Type.SIMPLE_PROPERTY, (g, v) -> g.writeObjectField("$eq", v)),
    NOT_EQUALS(Part.Type.NEGATING_SIMPLE_PROPERTY, (g, v) -> g.writeObjectField("$ne", v)),
    GREATER_THAN(Part.Type.GREATER_THAN, (g, v) -> g.writeObjectField("$gt", v)),
    GREATER_THAN_OR_EQUALS(Part.Type.GREATER_THAN_EQUAL, (g, v) -> g.writeObjectField("$gte", v)),
    LESSER_THAN(Part.Type.LESS_THAN, (g, v) -> g.writeObjectField("$lt", v)),
    LESSER_THAN_OR_EQUALS(Part.Type.LESS_THAN_EQUAL, (g, v) -> g.writeObjectField("$lte", v)),
    REGEX(Part.Type.REGEX, (g, v) -> g.writeObjectField("$regex", v)),
    NOT_NULL(Part.Type.IS_NOT_NULL, (g, v) -> g.writeObjectField("$ne", null)),
    NULL(Part.Type.IS_NULL, (g, v) -> g.writeObjectField("$eq", null)),
    BEFORE(Part.Type.BEFORE, (g, v) -> g.writeObjectField("$lt", v)),
    AFTER(Part.Type.AFTER, (g, v) -> g.writeObjectField("$gt", v)),
    STARTING_WITH(Part.Type.STARTING_WITH, (g, v) -> g.writeObjectField("$regex", "^" + v)),
    ENDING_WITH(Part.Type.ENDING_WITH, (g, v) -> g.writeObjectField("$regex", v + "$")),
    EMPTY(Part.Type.IS_EMPTY, (g, v) -> g.writeObjectField("$size", 0)),
    NOT_EMPTY(Part.Type.IS_NOT_EMPTY, (g, v) -> {
        g.writeFieldName("$not");
        g.writeRaw(":{\"$size\":0}");
    }),
    CONTAINING(Part.Type.CONTAINING, (g, v) -> g.writeObjectField("$regex", v)),
    NOT_CONTAINING(Part.Type.NOT_CONTAINING, (g, v) -> g.writeObjectField("$regex", "^((?!" + v + ").)*$")),
    LIKE(Part.Type.LIKE, (g, v) -> g.writeObjectField("$regex", "^" + v)),
    NOT_LIKE(Part.Type.NOT_LIKE, (g, v) -> g.writeObjectField("$regex", "^((?!" + v + ").)*$")),
    IN(Part.Type.IN, (g, v) -> g.writeObjectField("$in", v)),
    NOT_IN(Part.Type.NOT_IN, (g, v) -> g.writeObjectField("$nin", v)),
    TRUE(Part.Type.TRUE, (g, v) -> g.writeObjectField("$eq", true)),
    FALSE(Part.Type.FALSE, (g, v) -> g.writeObjectField("$eq", false)),
    BETWEEN(Part.Type.BETWEEN, (g, v) -> {
        throw new IllegalArgumentException("BETWEEN is not implemented yet");
        //g.writeObjectField("$allMatch", ":{\"$gt\": #from#, \"$lt\": #to#}"))
    }),
    WITHIN(Part.Type.WITHIN, (g, v) -> {
        throw new IllegalArgumentException("WITHIN is not implemented yet");
    }),
    NEAR(Part.Type.NEAR, (g, v) -> {
        throw new IllegalArgumentException("NEAR is not implemented yet");
    });

    private static final Map<Part.Type, Operation> translationMap = new HashMap<>();

    static {
        Arrays.stream(Operation.values()).forEach(o -> translationMap.put(o.getType(), o));
    }

    private final Part.Type type;
    private final ThrowingBiConsumer<JsonGenerator, Object, IOException> ruleWriter;

    Operation(@NotNull Part.Type type, @NotNull ThrowingBiConsumer<JsonGenerator, Object, IOException> ruleWriter) {
        this.type = type;
        this.ruleWriter = ruleWriter;
    }

    /**
     * @return {@link Part.Type} variation of the operation. Can not be {@literal null}
     */
    public @NotNull Part.Type getType() {
        return type;
    }

    /**
     * Method to serialize the operation thru the given {@link JsonGenerator}.
     *
     * @param value     Actual value of parameter for the operation
     * @param generator to which the operation is written in. Must not be {@literal null}
     * @throws IOException in cases of problem with serialization of the operation of its parameter
     */
    public void write(Object value, @NotNull JsonGenerator generator) throws IOException {
        ruleWriter.accept(generator, value);
    }

    /**
     * Method to obtain translation between {@link Part.Type} and {@link Operation}. If the {@link Part.Type} is unknown {@link IllegalStateException} is thrown
     *
     * @param type must not be {@literal null}
     * @return result of indexed lookup for Mango variation of the same {@link Part.Type} operation. Can not be {@literal null}
     */
    public static @NotNull Operation of(@NotNull Part.Type type) {
        Optional<Operation> optional = Optional.ofNullable(translationMap.get(type));
        return optional.orElseThrow(() -> new IllegalStateException("Unknown equivalent couchDb operation for " + type));
    }

}

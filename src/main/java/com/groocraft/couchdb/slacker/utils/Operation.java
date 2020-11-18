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
import org.jetbrains.annotations.Nullable;
import org.springframework.data.repository.query.parser.Part;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Enum to map {@link Part.Type} operation to CouchDB Mango query operators and javascript for automated creation of views from query methods.
 *
 * @author Majlanky
 */
public enum Operation {

    EQUALS(Part.Type.SIMPLE_PROPERTY, (g, v) -> g.writeObjectField("$eq", v), (k, v) -> replace("(%1$s == %2$s)", k, v)),
    NOT_EQUALS(Part.Type.NEGATING_SIMPLE_PROPERTY, (g, v) -> g.writeObjectField("$ne", v), (k, v) -> replace("(%1$s != %2$s)", k, v)),
    GREATER_THAN(Part.Type.GREATER_THAN, (g, v) -> g.writeObjectField("$gt", v), (k, v) -> replace("(%1$s > %2$s)", k, v)),
    GREATER_THAN_OR_EQUALS(Part.Type.GREATER_THAN_EQUAL, (g, v) -> g.writeObjectField("$gte", v), (k, v) -> replace("(%1$s >= %2$s)", k, v)),
    LESSER_THAN(Part.Type.LESS_THAN, (g, v) -> g.writeObjectField("$lt", v), (k, v) -> replace("(%1$s < %2$s)", k, v)),
    LESSER_THAN_OR_EQUALS(Part.Type.LESS_THAN_EQUAL, (g, v) -> g.writeObjectField("$lte", v), (k, v) -> replace("(%1$s <= %2$s)", k, v)),
    REGEX(Part.Type.REGEX, (g, v) -> g.writeObjectField("$regex", v), (k, v) -> format("(/%2$s/.test(%1$s))", k, v)),
    NOT_NULL(Part.Type.IS_NOT_NULL, (g, v) -> g.writeObjectField("$ne", null), (k, v) -> replace("(%1$s != null)", k, v)),
    NULL(Part.Type.IS_NULL, (g, v) -> g.writeObjectField("$eq", null), (k, v) -> replace("(%1$s == null)", k, v)),
    BEFORE(Part.Type.BEFORE, (g, v) -> g.writeObjectField("$lt", v), (k, v) -> replace("(%1$s < %2$s)", k, v)),
    AFTER(Part.Type.AFTER, (g, v) -> g.writeObjectField("$gt", v), (k, v) -> replace("(%1$s > %2$s)", k, v)),
    STARTING_WITH(Part.Type.STARTING_WITH, (g, v) -> g.writeObjectField("$regex", "^" + v), (k, v) -> replace("(%1$s.startsWith(%2$s))", k, v)),
    ENDING_WITH(Part.Type.ENDING_WITH, (g, v) -> g.writeObjectField("$regex", v + "$"), (k, v) -> replace("(%1$s.endsWith(%2$s))", k, v)),
    EMPTY(Part.Type.IS_EMPTY, (g, v) -> g.writeObjectField("$size", 0), (k, v) -> replace("(%1$s.length == 0)", k, v)),
    NOT_EMPTY(Part.Type.IS_NOT_EMPTY, (g, v) -> {
        g.writeFieldName("$not");
        g.writeRaw(":{\"$size\":0}");
    }, (k, v) -> replace("(%1$s.length != 0)", k, v)),
    CONTAINING(Part.Type.CONTAINING, (g, v) -> g.writeObjectField("$regex", v), (k, v) -> replace("(%1$s.includes(%2$s))", k, v)),
    NOT_CONTAINING(Part.Type.NOT_CONTAINING, (g, v) -> g.writeObjectField("$regex", "^((?!" + v + ").)*$"), (k, v) -> replace("(!%1$s.includes(%2$s))", k, v)),
    LIKE(Part.Type.LIKE, (g, v) -> g.writeObjectField("$regex", "^" + v), (k, v) -> replace("(%1$s.startsWith(%2$s))", k, v)),
    NOT_LIKE(Part.Type.NOT_LIKE, (g, v) -> g.writeObjectField("$regex", "^((?!" + v + ").)*$"), (k, v) -> replace("(!%1$s.includes(%2$s))", k, v)),
    IN(Part.Type.IN, (g, v) -> g.writeObjectField("$in", v), (k, v) -> replace("(%2$s.includes(%1$s))", k, v)),
    NOT_IN(Part.Type.NOT_IN, (g, v) -> g.writeObjectField("$nin", v), (k, v) -> replace("(!%2$s.includes(%1$s))", k, v)),
    TRUE(Part.Type.TRUE, (g, v) -> g.writeObjectField("$eq", true), (k, v) -> replace("(%1$s == true)", k, v)),
    FALSE(Part.Type.FALSE, (g, v) -> g.writeObjectField("$eq", false), (k, v) -> replace("(%1$s == false)", k, v)),
    BETWEEN(Part.Type.BETWEEN, (g, v) -> {
        throw new IllegalArgumentException("BETWEEN is not implemented yet");
        //g.writeObjectField("$allMatch", ":{\"$gt\": #from#, \"$lt\": #to#}"))
    }, (k, v) -> {
        throw new IllegalArgumentException("BETWEEN is not implemented yet");
    }),
    WITHIN(Part.Type.WITHIN, (g, v) -> {
        throw new IllegalArgumentException("WITHIN is not implemented yet");
    }, (k, v) -> {
        throw new IllegalArgumentException("WITHIN is not implemented yet");
    }),
    NEAR(Part.Type.NEAR, (g, v) -> {
        throw new IllegalArgumentException("NEAR is not implemented yet");
    }, (k, v) -> {
        throw new IllegalArgumentException("NEAR is not implemented yet");
    });

    private static final Map<Part.Type, Operation> translationMap = new HashMap<>();

    static {
        Arrays.stream(Operation.values()).forEach(o -> translationMap.put(o.getType(), o));
    }

    private final Part.Type type;
    private final ThrowingBiConsumer<JsonGenerator, Object, IOException> ruleWriter;
    private final BiFunction<String, Object, String> jsGenerator;

    /**
     * @param type        {@link Part.Type} of operation. Must not be {@literal null}
     * @param ruleWriter  {@link ThrowingBiConsumer} which do serialization of operation to given {@link JsonGenerator}. Must not be {@literal null}
     * @param jsGenerator {@link BiFunction} consuming attribute key and tested value and returns javascript condition of the operation
     */
    Operation(@NotNull Part.Type type, @NotNull ThrowingBiConsumer<JsonGenerator, Object, IOException> ruleWriter,
              @NotNull BiFunction<String, Object, String> jsGenerator) {
        Assert.notNull(type, "Type must not be null.");
        Assert.notNull(ruleWriter, "RuleWriter must not be null");
        this.type = type;
        this.ruleWriter = ruleWriter;
        this.jsGenerator = jsGenerator;
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
     * @param key   must not be {@literal null}
     * @param value must not be {@literal null}
     * @return javascript condition of the operation with the given key and value
     */
    public @NotNull String jsCondition(@NotNull String key, @Nullable Object value) {
        return jsGenerator.apply(key, value);
    }

    private static String format(String pattern, String key, Object value) {
        return String.format(pattern, "doc." + key, value);
    }

    private static String replace(String pattern, String key, Object value) {
        String v = value != null ? value.toString() : null;
        if (value instanceof String) {
            v = "\"" + value + "\"";
        }
        if (value instanceof Iterable) {
            v = "[" + StreamSupport.stream(((Iterable<?>) value).spliterator(), false).
                    map(e -> e instanceof String ? "\"" + e + "\"" : e.toString()).
                    collect(Collectors.joining(",")) + "]";
        }
        return format(pattern, key, v);
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

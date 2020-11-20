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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

/**
 * Enum to map {@link Part.Type} operation to CouchDB Mango query operators and javascript for automated creation of views from query methods.
 *
 * @author Majlanky
 */
public enum Operation {

    EQUALS(Part.Type.SIMPLE_PROPERTY, (g, v) -> g.writeObjectField("$eq", v), (k, v, m) -> formatWithObjectStringification("(%1$s == %2$s)", k, v, m)),
    NOT_EQUALS(Part.Type.NEGATING_SIMPLE_PROPERTY, (g, v) -> g.writeObjectField("$ne", v), (k, v, m) -> formatWithObjectStringification("(%1$s != %2$s)", k, v, m)),
    GREATER_THAN(Part.Type.GREATER_THAN, (g, v) -> g.writeObjectField("$gt", v), (k, v, m) -> format("(%1$s > %2$s)", k, v, m)),
    GREATER_THAN_OR_EQUALS(Part.Type.GREATER_THAN_EQUAL, (g, v) -> g.writeObjectField("$gte", v), (k, v, m) -> format("(%1$s >= %2$s)", k, v, m)),
    LESSER_THAN(Part.Type.LESS_THAN, (g, v) -> g.writeObjectField("$lt", v), (k, v, m) -> format("(%1$s < %2$s)", k, v, m)),
    LESSER_THAN_OR_EQUALS(Part.Type.LESS_THAN_EQUAL, (g, v) -> g.writeObjectField("$lte", v), (k, v, m) -> format("(%1$s <= %2$s)", k, v, m)),
    REGEX(Part.Type.REGEX, (g, v) -> g.writeObjectField("$regex", v), Operation::formatRegex),
    NOT_NULL(Part.Type.IS_NOT_NULL, (g, v) -> g.writeObjectField("$ne", null), (k, v, m) -> format("(%1$s != null)", k, v, m)),
    NULL(Part.Type.IS_NULL, (g, v) -> g.writeObjectField("$eq", null), (k, v, m) -> format("(%1$s == null)", k, v, m)),
    BEFORE(Part.Type.BEFORE, (g, v) -> g.writeObjectField("$lt", v), (k, v, m) -> format("(%1$s < %2$s)", k, v, m)),
    AFTER(Part.Type.AFTER, (g, v) -> g.writeObjectField("$gt", v), (k, v, m) -> format("(%1$s > %2$s)", k, v, m)),
    STARTING_WITH(Part.Type.STARTING_WITH, (g, v) -> g.writeObjectField("$regex", "^" + v), (k, v, m) -> format("(%1$s.startsWith(%2$s))", k, v, m)),
    ENDING_WITH(Part.Type.ENDING_WITH, (g, v) -> g.writeObjectField("$regex", v + "$"), (k, v, m) -> format("(%1$s.endsWith(%2$s))", k, v, m)),
    EMPTY(Part.Type.IS_EMPTY, (g, v) -> g.writeObjectField("$size", 0), (k, v, m) -> format("(%1$s.length == 0)", k, v, m)),
    NOT_EMPTY(Part.Type.IS_NOT_EMPTY, (g, v) -> {
        g.writeFieldName("$not");
        g.writeRaw(":{\"$size\":0}");
    }, (k, v, m) -> format("(%1$s.length != 0)", k, v, m)),
    CONTAINING(Part.Type.CONTAINING, (g, v) -> g.writeObjectField("$regex", v), (k, v, m) -> format("(%1$s.includes(%2$s))", k, v, m)),
    NOT_CONTAINING(Part.Type.NOT_CONTAINING, (g, v) -> g.writeObjectField("$regex", "^((?!" + v + ").)*$"), (k, v, m) -> format("(!%1$s.includes(%2$s))", k, v, m)),
    LIKE(Part.Type.LIKE, (g, v) -> g.writeObjectField("$regex", "^" + v), (k, v, m) -> format("(%1$s.startsWith(%2$s))", k, v, m)),
    NOT_LIKE(Part.Type.NOT_LIKE, (g, v) -> g.writeObjectField("$regex", "^((?!" + v + ").)*$"), (k, v, m) -> format("(!%1$s.includes(%2$s))", k, v, m)),
    IN(Part.Type.IN, (g, v) -> g.writeObjectField("$in", v), (k, v, m) -> format("(%2$s.includes(%1$s))", k, v, m)),
    NOT_IN(Part.Type.NOT_IN, (g, v) -> g.writeObjectField("$nin", v), (k, v, m) -> format("(!%2$s.includes(%1$s))", k, v, m)),
    TRUE(Part.Type.TRUE, (g, v) -> g.writeObjectField("$eq", true), (k, v, m) -> format("(%1$s == true)", k, v, m)),
    FALSE(Part.Type.FALSE, (g, v) -> g.writeObjectField("$eq", false), (k, v, m) -> format("(%1$s == false)", k, v, m)),
    BETWEEN(Part.Type.BETWEEN, (g, v) -> {
        throw new IllegalArgumentException("BETWEEN is not implemented yet");
        //g.writeObjectField("$allMatch", ":{\"$gt\": #from#, \"$lt\": #to#}"))
    }, (k, v, m) -> {
        throw new IllegalArgumentException("BETWEEN is not implemented yet");
    }),
    WITHIN(Part.Type.WITHIN, (g, v) -> {
        throw new IllegalArgumentException("WITHIN is not implemented yet");
    }, (k, v, m) -> {
        throw new IllegalArgumentException("WITHIN is not implemented yet");
    }),
    NEAR(Part.Type.NEAR, (g, v) -> {
        throw new IllegalArgumentException("NEAR is not implemented yet");
    }, (k, v, m) -> {
        throw new IllegalArgumentException("NEAR is not implemented yet");
    });

    private static final Map<Part.Type, Operation> translationMap = new HashMap<>();

    static {
        Arrays.stream(Operation.values()).forEach(o -> translationMap.put(o.getType(), o));
    }

    private final Part.Type type;
    private final ThrowingBiConsumer<JsonGenerator, Object, IOException> ruleWriter;
    private final ThrowingTriFunction<String, Object, ObjectMapper, String, JsonProcessingException> jsGenerator;

    /**
     * @param type        {@link Part.Type} of operation. Must not be {@literal null}
     * @param ruleWriter  {@link ThrowingBiConsumer} which do serialization of operation to given {@link JsonGenerator}. Must not be {@literal null}
     * @param jsGenerator {@link BiFunction} consuming attribute key and tested value and returns javascript condition of the operation
     */
    Operation(@NotNull Part.Type type, @NotNull ThrowingBiConsumer<JsonGenerator, Object, IOException> ruleWriter,
              @NotNull ThrowingTriFunction<String, Object, ObjectMapper, String, JsonProcessingException> jsGenerator) {
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
     * Methods returns javascript condition of the operation for the given key and value.
     *
     * @param key          must not be {@literal null}
     * @param value        of the condition
     * @param objectMapper used for serialization of value. Must not be {@literal null}
     * @return javascript condition of the operation with the given key and value
     * @throws JsonProcessingException in case of inability for serialize the given value
     */
    public @NotNull String jsCondition(@NotNull String key, @Nullable Object value, @NotNull ObjectMapper objectMapper) throws JsonProcessingException {
        return jsGenerator.apply(key, value, objectMapper);
    }

    /**
     * Methods returns javascript condition of the operation for the given key and value. If the given value is object or array in javascript, both the key
     * and value are wrapped into JSON.stringify to provide deep comparison.
     *
     * @param pattern      of the condition. Must not be {@literal null}
     * @param key          must not be {@literal null}
     * @param value        of the condition
     * @param objectMapper used for serialization of value. Must not be {@literal null}
     * @return javascript condition of the operation with the given key and value
     * @throws JsonProcessingException in case of inability for serialize the given value
     */
    private static String formatWithObjectStringification(@NotNull String pattern, @NotNull String key, @Nullable Object value,
                                                          @NotNull ObjectMapper objectMapper) throws JsonProcessingException {
        String serializedValue = objectMapper.writeValueAsString(value);
        key = "doc." + key;
        if (serializedValue.startsWith("{") || serializedValue.startsWith("[")) {
            serializedValue = "JSON.stringify(" + serializedValue + ")";
            key = "JSON.stringify(" + key + ")";
        }
        return String.format(pattern, key, serializedValue);
    }

    /**
     * Methods returns javascript condition of the operation for the given key and value. If deep comparison is needed, use
     * {@link #formatWithObjectStringification(String, String, Object, ObjectMapper)}
     *
     * @param pattern      of the condition. Must not be {@literal null}
     * @param key          must not be {@literal null}
     * @param value        of the condition
     * @param objectMapper used for serialization of value. Must not be {@literal null}
     * @return javascript condition of the operation with the given key and value
     * @throws JsonProcessingException in case of inability for serialize the given value
     */
    private static String format(@NotNull String pattern, @NotNull String key, @Nullable Object value, @NotNull ObjectMapper objectMapper) throws JsonProcessingException {
        return String.format(pattern, "doc." + key, objectMapper.writeValueAsString(value));
    }

    /**
     * Special format method for javascript regex-based conditions.
     *
     * @param key          must not be {@literal null}
     * @param value        of the condition
     * @param objectMapper used for serialization of value. Must not be {@literal null}
     * @return javascript condition of the operation with the given key and value
     */
    private static String formatRegex(@NotNull String key, @Nullable Object value, @NotNull ObjectMapper objectMapper) {
        return String.format("(/%2$s/.test(%1$s))", "doc." + key, value);
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

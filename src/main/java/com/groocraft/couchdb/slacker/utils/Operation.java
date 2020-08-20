package com.groocraft.couchdb.slacker.utils;

import org.springframework.data.repository.query.parser.Part;

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

    //TODO support all
    EQUALS(Part.Type.SIMPLE_PROPERTY, "eq"),
    GREATER_THAN(Part.Type.GREATER_THAN, "gt"),
    GREATER_THAN_OR_EQUALS(Part.Type.GREATER_THAN_EQUAL, "gte"),
    LESSER_THAN(Part.Type.LESS_THAN, "lt"),
    LESSER_THAN_OR_EQUALS(Part.Type.LESS_THAN_EQUAL, "lte"),
    REGEX(Part.Type.REGEX, "regex");

    private static final Map<Part.Type, String> translationMap = new HashMap<>();

    static {
        Arrays.stream(Operation.values()).forEach(o -> translationMap.put(o.getType(), o.getValue()));
    }

    private final String value;
    private final Part.Type type;

    Operation(Part.Type type, String value) {
        this.value = "$" + value;
        this.type = type;
    }

    /**
     * @return Mango variation of operation in format $something.
     */
    public String getValue() {
        return value;
    }

    /**
     * @return {@link Part.Type} variation of the operation
     */
    public Part.Type getType() {
        return type;
    }

    /**
     * @param type must not be {@literal null}
     * @return result of indexed lookup for Mango variation of the same {@link Part.Type} operation.
     */
    public static String get(Part.Type type) {
        Optional<String> optional = Optional.ofNullable(translationMap.get(type));
        return optional.orElseThrow(() -> new IllegalStateException("Unknown equivalent couchDb operation for " + type));
    }
}

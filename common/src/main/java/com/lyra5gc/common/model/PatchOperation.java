package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * PatchOperation — TS 29.510 Table 6.1.6.3.11-1 (RFC 6902 subset).
 */
public enum PatchOperation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private final String value;

    PatchOperation(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static PatchOperation fromString(String s) {
        if (s == null) {
            return null;
        }
        for (PatchOperation t : values()) {
            if (t.value.equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}

package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * UriScheme — TS 29.510 Table 6.1.6.3.8-1.
 */
public enum UriScheme {
    HTTP("http"),
    HTTPS("https");

    private final String value;

    UriScheme(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static UriScheme fromString(String s) {
        if (s == null) {
            return null;
        }
        for (UriScheme t : values()) {
            if (t.value.equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}

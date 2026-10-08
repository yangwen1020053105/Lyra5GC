package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * NFServiceStatus — TS 29.510 Table 6.1.6.3.14-1.
 */
public enum NfServiceStatus {
    REGISTERED,
    SUSPENDED,
    UNDISCOVERABLE;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static NfServiceStatus fromString(String s) {
        if (s == null) {
            return null;
        }
        for (NfServiceStatus t : values()) {
            if (t.name().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}

package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * NFStatus — TS 29.510 Table 6.1.6.3.4-1.
 */
public enum NfStatus {
    REGISTERED,
    SUSPENDED,
    UNDISCOVERABLE;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static NfStatus fromString(String s) {
        if (s == null) {
            return null;
        }
        for (NfStatus t : values()) {
            if (t.name().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}

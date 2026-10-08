package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * TransportProtocol — TS 29.510 Table 6.1.6.3.9-1.
 */
public enum TransportProtocol {
    TCP,
    SCTP,
    UDP;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static TransportProtocol fromString(String s) {
        if (s == null) {
            return null;
        }
        for (TransportProtocol t : values()) {
            if (t.name().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}

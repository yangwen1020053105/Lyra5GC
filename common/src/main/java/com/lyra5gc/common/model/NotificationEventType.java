package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * NotificationEventType — TS 29.510 Table 6.1.6.3.5-1.
 */
public enum NotificationEventType {
    NF_REGISTERED,
    NF_DEREGISTERED,
    NF_PROFILE_CHANGED;

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static NotificationEventType fromString(String s) {
        if (s == null) {
            return null;
        }
        for (NotificationEventType t : values()) {
            if (t.name().equalsIgnoreCase(s)) {
                return t;
            }
        }
        return null;
    }
}

package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DefaultNotificationSubscription — TS 29.510 Table 6.1.6.2.11-1 (subset).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefaultNotificationSubscription {

    private String notificationType;
    private String callbackUri;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getCallbackUri() {
        return callbackUri;
    }

    public void setCallbackUri(String callbackUri) {
        this.callbackUri = callbackUri;
    }
}

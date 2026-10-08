package com.lyra5gc.nrf.context;

import com.lyra5gc.common.model.SubscriptionData;

import java.time.OffsetDateTime;
import java.util.concurrent.ScheduledFuture;

/**
 * Server-side state of an NF status subscription
 * (ogs_sbi_subscription_data_t in Open5GS).
 */
public class SubscriptionContext {

    private final String id;
    private final SubscriptionData data;
    private volatile ScheduledFuture<?> validityTimer;

    public SubscriptionContext(String id, SubscriptionData data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public SubscriptionData getData() {
        return data;
    }

    public String getNotificationUri() {
        return data.getNfStatusNotificationUri();
    }

    public ScheduledFuture<?> getValidityTimer() {
        return validityTimer;
    }

    public void setValidityTimer(ScheduledFuture<?> validityTimer) {
        this.validityTimer = validityTimer;
    }

    public void setValidityTime(OffsetDateTime time) {
        data.setValidityTime(time == null ? null : time.toString());
    }
}

package com.lyra5gc.nrf.registry;

import com.lyra5gc.nrf.context.SubscriptionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Registry of NF status subscriptions with validity timers —
 * mirrors Open5GS NRF_TIMER_SUBSCRIPTION_VALIDITY handling.
 */
@Component
public class SubscriptionRegistry {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionRegistry.class);

    private final Map<String, SubscriptionContext> subscriptions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;

    public SubscriptionRegistry(ScheduledExecutorService nrfScheduler) {
        this.scheduler = nrfScheduler;
    }

    public SubscriptionContext add(com.lyra5gc.common.model.SubscriptionData data, Duration validity) {
        String id = UUID.randomUUID().toString();
        data.setSubscriptionId(id);
        SubscriptionContext ctx = new SubscriptionContext(id, data);
        subscriptions.put(id, ctx);
        refreshValidity(ctx, validity);
        log.info("[{}] Subscription created [validity:{}s]", id, validity.toSeconds());
        return ctx;
    }

    public SubscriptionContext find(String id) {
        return subscriptions.get(id);
    }

    public Collection<SubscriptionContext> list() {
        return subscriptions.values();
    }

    public boolean remove(String id) {
        SubscriptionContext ctx = subscriptions.remove(id);
        if (ctx == null) {
            return false;
        }
        if (ctx.getValidityTimer() != null) {
            ctx.getValidityTimer().cancel(false);
        }
        return true;
    }

    /**
     * Extends the subscription validity (Nnrf_NFManagement NFStatusUpdate).
     */
    public void refreshValidity(SubscriptionContext ctx, Duration validity) {
        ScheduledFuture<?> old = ctx.getValidityTimer();
        if (old != null) {
            old.cancel(false);
        }
        ctx.setValidityTime(java.time.OffsetDateTime.now().plus(validity));
        ctx.setValidityTimer(scheduler.schedule(() -> {
            log.warn("[{}] Subscription validity expired", ctx.getId());
            subscriptions.remove(ctx.getId());
        }, validity.toMillis(), TimeUnit.MILLISECONDS));
    }
}

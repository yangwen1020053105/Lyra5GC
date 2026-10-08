package com.lyra5gc.nrf.registry;

import com.lyra5gc.common.model.NfStatus;
import com.lyra5gc.nrf.config.NrfProperties;
import com.lyra5gc.nrf.context.NfInstanceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Registry of registered NF instances.
 *
 * Mirrors the NRF-side lifecycle of Open5GS src/nrf/nf-sm.c:
 *  - registered state starts a no-heartbeat timer (heartBeatTimer + margin)
 *  - every PUT/PATCH on the instance restarts the timer
 *  - on expiry the NF is marked SUSPENDED and removed, subscribers are
 *    notified with NF_DEREGISTERED (via {@link #setOnRemoved} callback)
 */
@Component
public class NfRegistry {

    private static final Logger log = LoggerFactory.getLogger(NfRegistry.class);

    private final Map<String, NfInstanceContext> instances = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final NrfProperties properties;

    /** Invoked (synchronously) when an instance is removed: deregistered or timed out. */
    private volatile Consumer<NfInstanceContext> onRemoved = ctx -> {
    };
    /** Invoked after a new instance has been registered. */
    private volatile Consumer<NfInstanceContext> onRegistered = ctx -> {
    };

    public NfRegistry(ScheduledExecutorService nrfScheduler, NrfProperties properties) {
        this.scheduler = nrfScheduler;
        this.properties = properties;
    }

    public void setOnRemoved(Consumer<NfInstanceContext> onRemoved) {
        this.onRemoved = onRemoved;
    }

    public void setOnRegistered(Consumer<NfInstanceContext> onRegistered) {
        this.onRegistered = onRegistered;
    }

    /**
     * @return true if this is a new registration (201), false if an update (200)
     */
    public boolean register(NfInstanceContext ctx) {
        boolean created = !instances.containsKey(ctx.getId());
        NfInstanceContext old = instances.put(ctx.getId(), ctx);
        if (old != null && old.getNoHeartbeatTimer() != null) {
            old.getNoHeartbeatTimer().cancel(false);
        }
        restartNoHeartbeatTimer(ctx);
        if (created) {
            onRegistered.accept(ctx);
        }
        log.info("[{}] NF {}registered [type:{} heartbeat:{}s]", ctx.getId(),
                created ? "" : "re-", ctx.getProfile().getNfType(), ctx.getHeartbeatInterval());
        return created;
    }

    /**
     * Restarts the no-heartbeat timer (PUT/PATCH/heartbeat on a registered NF).
     */
    public void touch(String nfInstanceId) {
        NfInstanceContext ctx = instances.get(nfInstanceId);
        if (ctx != null) {
            restartNoHeartbeatTimer(ctx);
        }
    }

    public NfInstanceContext find(String nfInstanceId) {
        return instances.get(nfInstanceId);
    }

    public Collection<NfInstanceContext> list() {
        return instances.values();
    }

    /**
     * Removes the instance (explicit NF Deregister or heartbeat expiry).
     */
    public boolean remove(String nfInstanceId) {
        NfInstanceContext ctx = instances.remove(nfInstanceId);
        if (ctx == null) {
            return false;
        }
        if (ctx.getNoHeartbeatTimer() != null) {
            ctx.getNoHeartbeatTimer().cancel(false);
        }
        onRemoved.accept(ctx);
        return true;
    }

    private void restartNoHeartbeatTimer(NfInstanceContext ctx) {
        int interval = ctx.getHeartbeatInterval();
        if (interval <= 0) {
            return;
        }
        ScheduledFuture<?> old = ctx.getNoHeartbeatTimer();
        if (old != null) {
            old.cancel(false);
        }
        long delay = interval + properties.getNoHeartbeatMargin();
        ctx.setNoHeartbeatTimer(scheduler.schedule(() -> {
            log.warn("[{}] No heartbeat", ctx.getId());
            ctx.getProfile().setNfStatus(NfStatus.SUSPENDED);
            remove(ctx.getId());
        }, delay, TimeUnit.SECONDS));
    }
}

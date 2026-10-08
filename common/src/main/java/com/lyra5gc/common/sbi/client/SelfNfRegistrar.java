package com.lyra5gc.common.sbi.client;

import com.lyra5gc.common.model.NFProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * NF-side registration lifecycle: NFRegister on start, periodic HeartBeat,
 * NFDeregister on stop — mirrors ogs_sbi_nf_fsm registered state in Open5GS
 * (lib/sbi/nf-sm.c) combined with the NRF-configured heartbeat interval.
 */
public class SelfNfRegistrar {

    private static final Logger log = LoggerFactory.getLogger(SelfNfRegistrar.class);

    private final NfRegistrationClient client;
    private final NFProfile profile;
    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "nf-heartbeat");
                t.setDaemon(true);
                return t;
            });

    public SelfNfRegistrar(NfRegistrationClient client, NFProfile profile) {
        this.client = client;
        this.profile = profile;
    }

    public NFProfile getProfile() {
        return profile;
    }

    /** Registers to the NRF and starts the heartbeat loop. */
    public void start() {
        client.register(profile)
                .doOnSuccess(registered -> startHeartbeat(heartbeatInterval(registered)))
                .doOnError(e -> log.warn("Register failed, retrying in 5s: {}", e.getMessage()))
                .onErrorResume(e -> {
                    scheduleRetry();
                    return reactor.core.publisher.Mono.empty();
                })
                .subscribe();
    }

    /** NFDeregister (best effort, blocking briefly for graceful shutdown). */
    public void stop() {
        scheduler.shutdownNow();
        try {
            client.deregister(profile.getNfInstanceId())
                    .block(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.warn("NFDeregister failed on shutdown: {}", e.getMessage());
        }
    }

    /** Triggers one heartbeat immediately (used by test endpoints). */
    public reactor.core.publisher.Mono<Void> heartbeatNow() {
        return client.heartbeat(profile.getNfInstanceId(),
                profile.getLoad() != null ? profile.getLoad() : 0);
    }

    private int heartbeatInterval(NFProfile registered) {
        Integer hb = registered != null ? registered.getHeartBeatTimer() : null;
        return hb != null && hb > 0 ? hb : 10;
    }

    private void startHeartbeat(int intervalSec) {
        scheduler.scheduleWithFixedDelay(() -> {
            try {
                heartbeatNow().block(Duration.ofSeconds(intervalSec));
            } catch (Exception e) {
                /*
                 * Heartbeat failed (e.g. the NRF evicted us after a timer
                 * race, or the NRF restarted): re-register, like the Open5GS
                 * NF FSM falling back from registered to will_register.
                 */
                log.warn("[{}] Heartbeat failed ({}), re-registering...",
                        profile.getNfInstanceId(), e.getMessage());
                try {
                    client.register(profile).block(Duration.ofSeconds(10));
                } catch (Exception re) {
                    log.warn("[{}] Re-register failed: {}",
                            profile.getNfInstanceId(), re.getMessage());
                }
            }
        }, intervalSec, intervalSec, TimeUnit.SECONDS);
        log.info("[{}] Heartbeat scheduled every {}s", profile.getNfInstanceId(), intervalSec);
    }

    private void scheduleRetry() {
        scheduler.schedule(this::start, 5, TimeUnit.SECONDS);
    }
}

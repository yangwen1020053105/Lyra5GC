package com.lyra5gc.ausf.service;

import com.lyra5gc.common.sbi.client.SelfNfRegistrar;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Registers the AUSF to the NRF once the application is ready,
 * deregisters on shutdown.
 */
@Service
public class AusfRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(AusfRegistrationService.class);

    private final SelfNfRegistrar registrar;

    public AusfRegistrationService(SelfNfRegistrar registrar) {
        this.registrar = registrar;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Registering AUSF [{}] to NRF...",
                registrar.getProfile().getNfInstanceId());
        registrar.start();
    }

    @PreDestroy
    public void onShutdown() {
        registrar.stop();
    }
}

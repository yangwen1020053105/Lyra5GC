package com.lyra5gc.amf.service;

import com.lyra5gc.common.sbi.client.SelfNfRegistrar;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Registers the AMF to the NRF once the application is ready,
 * deregisters on shutdown.
 */
@Service
public class AmfRegistrationService {

    private static final Logger log = LoggerFactory.getLogger(AmfRegistrationService.class);

    private final SelfNfRegistrar registrar;

    public AmfRegistrationService(SelfNfRegistrar registrar) {
        this.registrar = registrar;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("Registering AMF [{}] to NRF...",
                registrar.getProfile().getNfInstanceId());
        registrar.start();
    }

    @PreDestroy
    public void onShutdown() {
        registrar.stop();
    }
}

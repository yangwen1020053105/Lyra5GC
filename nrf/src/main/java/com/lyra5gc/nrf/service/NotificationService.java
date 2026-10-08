package com.lyra5gc.nrf.service;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NFService;
import com.lyra5gc.common.model.NotificationData;
import com.lyra5gc.common.model.NotificationEventType;
import com.lyra5gc.common.model.SubscrCond;
import com.lyra5gc.common.sbi.SbiConstants;
import com.lyra5gc.nrf.config.NrfProperties;
import com.lyra5gc.nrf.context.NfInstanceContext;
import com.lyra5gc.nrf.context.SubscriptionContext;
import com.lyra5gc.nrf.registry.NfRegistry;
import com.lyra5gc.nrf.registry.SubscriptionRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Sends Nnrf_NFManagement NFStatusNotify notifications —
 * mirrors Open5GS nrf_nnrf_nfm_send_nf_status_notify_all / nnrf-build.c.
 */
@Component
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final WebClient sbiWebClient;
    private final NfRegistry nfRegistry;
    private final SubscriptionRegistry subscriptionRegistry;
    private final NrfProperties properties;

    public NotificationService(WebClient sbiWebClient,
                               NfRegistry nfRegistry,
                               SubscriptionRegistry subscriptionRegistry,
                               NrfProperties properties) {
        this.sbiWebClient = sbiWebClient;
        this.nfRegistry = nfRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        nfRegistry.setOnRegistered(ctx -> notifyAll(NotificationEventType.NF_REGISTERED, ctx));
        nfRegistry.setOnRemoved(ctx -> notifyAll(NotificationEventType.NF_DEREGISTERED, ctx));
    }

    /**
     * TS 29.510 5.2.2.5.2: on subscription creation, the NRF notifies the
     * subscriber about all already-registered NFs matching the condition.
     */
    public void notifyExistingMatches(SubscriptionContext subscription) {
        for (NfInstanceContext ctx : nfRegistry.list()) {
            if (matches(subscription, ctx)) {
                send(subscription, NotificationEventType.NF_REGISTERED, ctx);
            }
        }
    }

    public void notifyAll(NotificationEventType event, NfInstanceContext nfInstance) {
        for (SubscriptionContext subscription : subscriptionRegistry.list()) {
            if (matches(subscription, nfInstance)) {
                send(subscription, event, nfInstance);
            }
        }
    }

    /**
     * SubscrCond matching: nfType | serviceName | nfInstanceId.
     */
    private boolean matches(SubscriptionContext subscription, NfInstanceContext nfInstance) {
        SubscrCond cond = subscription.getData().getSubscrCond();
        if (cond == null) {
            return true;
        }
        NFProfile profile = nfInstance.getProfile();
        if (cond.getNfType() != null) {
            return cond.getNfType() == profile.getNfType();
        }
        if (cond.getServiceName() != null) {
            return profile.findService(cond.getServiceName()) != null;
        }
        if (cond.getNfInstanceId() != null) {
            return cond.getNfInstanceId().equals(nfInstance.getId());
        }
        return false;
    }

    private void send(SubscriptionContext subscription,
                      NotificationEventType event, NfInstanceContext nfInstance) {
        NotificationData data = new NotificationData();
        data.setEvent(event);
        data.setNfInstanceUri(properties.getApiRoot()
                + "/nnrf-nfm/" + SbiConstants.API_V1
                + "/" + SbiConstants.RESOURCE_NF_INSTANCES
                + "/" + nfInstance.getId());

        /* TS 29.510: nf_profile is not included for NF_DEREGISTERED */
        if (event != NotificationEventType.NF_DEREGISTERED) {
            data.setNfProfile(nfInstance.getProfile());
        }

        log.debug("Notify [{}] {} -> {}", subscription.getId(), event,
                subscription.getNotificationUri());

        sbiWebClient.post()
                .uri(subscription.getNotificationUri())
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.valueOf(SbiConstants.CONTENT_PROBLEM))
                .header(SbiConstants.HDR_CALLBACK,
                        SbiConstants.CALLBACK_NNRF_NFM_NF_STATUS_NOTIFY)
                .bodyValue(data)
                .retrieve()
                .toBodilessEntity()
                .subscribe(
                        resp -> log.debug("Notify [{}] done [{}]", subscription.getId(),
                                resp.getStatusCode()),
                        err -> log.warn("Notify [{}] failed: {}", subscription.getId(),
                                err.getMessage()));
    }
}

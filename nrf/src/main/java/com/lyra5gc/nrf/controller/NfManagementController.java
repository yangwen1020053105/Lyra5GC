package com.lyra5gc.nrf.controller;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NfInstancesDocument;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.PatchItem;
import com.lyra5gc.common.model.ProblemDetails;
import com.lyra5gc.common.model.SubscriptionData;
import com.lyra5gc.common.sbi.SbiConstants;
import com.lyra5gc.common.sbi.SbiJson;
import com.lyra5gc.nrf.config.NrfProperties;
import com.lyra5gc.nrf.context.NfInstanceContext;
import com.lyra5gc.nrf.context.SubscriptionContext;
import com.lyra5gc.nrf.registry.NfRegistry;
import com.lyra5gc.nrf.registry.SubscriptionRegistry;
import com.lyra5gc.nrf.service.NfManagementService;
import com.lyra5gc.nrf.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Nnrf_NFManagement service API — TS 29.510 clause 5.2.
 * Mirrors Open5GS src/nrf/nnrf-handler.c + nf-sm.c.
 */
@RestController
@RequestMapping("/nnrf-nfm/v1")
public class NfManagementController {

    private static final Logger log = LoggerFactory.getLogger(NfManagementController.class);
    private static final MediaType PROBLEM_JSON = MediaType.valueOf(SbiConstants.CONTENT_PROBLEM);
    private static final MediaType HAL_JSON = MediaType.valueOf(SbiConstants.CONTENT_3GPP_HAL);

    private final NfManagementService nfManagementService;
    private final NotificationService notificationService;
    private final NfRegistry nfRegistry;
    private final SubscriptionRegistry subscriptionRegistry;
    private final NrfProperties properties;

    public NfManagementController(NfManagementService nfManagementService,
                                  NotificationService notificationService,
                                  NfRegistry nfRegistry,
                                  SubscriptionRegistry subscriptionRegistry,
                                  NrfProperties properties) {
        this.nfManagementService = nfManagementService;
        this.notificationService = notificationService;
        this.nfRegistry = nfRegistry;
        this.subscriptionRegistry = subscriptionRegistry;
        this.properties = properties;
    }

    /* ------------------------------------------------------------------ */
    /* NFRegister — PUT /nf-instances/{nfInstanceId}                       */
    /* ------------------------------------------------------------------ */
    @PutMapping("/nf-instances/{nfInstanceId}")
    public ResponseEntity<NFProfile> nfRegister(@PathVariable String nfInstanceId,
                                                @RequestBody NFProfile nfProfile) {
        NfManagementService.RegisterResult result =
                nfManagementService.register(nfInstanceId, nfProfile);
        return ResponseEntity.status(result.httpStatus())
                .header(HttpHeaders.LOCATION, result.location())
                .contentType(MediaType.APPLICATION_JSON)
                .body(result.responseProfile());
    }

    /* ------------------------------------------------------------------ */
    /* NFUpdate / HeartBeat — PATCH /nf-instances/{nfInstanceId}           */
    /* ------------------------------------------------------------------ */
    @PatchMapping("/nf-instances/{nfInstanceId}")
    public ResponseEntity<Void> nfUpdate(@PathVariable String nfInstanceId,
                                         @RequestBody(required = false) List<PatchItem> patchItems) {
        nfManagementService.update(nfInstanceId,
                patchItems != null ? patchItems : List.of());
        return ResponseEntity.noContent().build();
    }

    /* ------------------------------------------------------------------ */
    /* NFDeregister — DELETE /nf-instances/{nfInstanceId}                  */
    /* ------------------------------------------------------------------ */
    @DeleteMapping("/nf-instances/{nfInstanceId}")
    public ResponseEntity<Void> nfDeregister(@PathVariable String nfInstanceId) {
        nfManagementService.deregister(nfInstanceId);
        return ResponseEntity.noContent().build();
    }

    /* ------------------------------------------------------------------ */
    /* NFListRetrieval — GET /nf-instances                                 */
    /* ------------------------------------------------------------------ */
    @GetMapping(value = "/nf-instances", produces = SbiConstants.CONTENT_3GPP_HAL)
    public ResponseEntity<NfInstancesDocument> nfListRetrieval(
            @RequestParam(value = "nf-type", required = false) NfType nfType,
            @RequestParam(value = "limit", required = false) Integer limit) {

        String self = properties.getApiRoot() + "/nnrf-nfm/v1/nf-instances";

        List<String> items = new ArrayList<>();
        int i = 0;
        for (NfInstanceContext ctx : nfRegistry.list()) {
            if (nfType != null && nfType != ctx.getProfile().getNfType()) {
                continue;
            }
            if (limit == null || i < limit) {
                items.add(self + "/" + ctx.getId());
            }
            i++;
        }
        return ResponseEntity.ok()
                .contentType(HAL_JSON)
                .body(new NfInstancesDocument(self, items));
    }

    /* ------------------------------------------------------------------ */
    /* NFProfileRetrieval — GET /nf-instances/{nfInstanceId}               */
    /* ------------------------------------------------------------------ */
    @GetMapping("/nf-instances/{nfInstanceId}")
    public ResponseEntity<NFProfile> nfProfileRetrieval(@PathVariable String nfInstanceId) {
        NfInstanceContext ctx = nfRegistry.find(nfInstanceId);
        if (ctx == null) {
            throw new NfManagementService.NfManagementException(
                    404, "Not found [" + nfInstanceId + "]");
        }
        return ResponseEntity.ok(ctx.getProfile());
    }

    /* ------------------------------------------------------------------ */
    /* NFStatusSubscribe — POST /subscriptions                             */
    /* nrf_nnrf_handle_nf_status_subscribe() in Open5GS                    */
    /* ------------------------------------------------------------------ */
    @PostMapping("/subscriptions")
    public ResponseEntity<SubscriptionData> nfStatusSubscribe(
            @RequestBody SubscriptionData subscriptionData) {

        if (subscriptionData.getNfStatusNotificationUri() == null) {
            throw new NfManagementService.NfManagementException(
                    400, "No nfStatusNotificationUri");
        }
        if (subscriptionData.getSubscrCond() == null) {
            throw new NfManagementService.NfManagementException(
                    400, "No SubscrCond found in NF Subscription message");
        }

        /* Issue #2630: SubscrCond is oneOf — nfType and serviceName are exclusive */
        var cond = subscriptionData.getSubscrCond();
        if (cond.getNfType() != null && cond.getServiceName() != null) {
            throw new NfManagementService.NfManagementException(
                    400, "SubscrCond must be either an NF type or a service name");
        }
        if (cond.getNfType() == null && cond.getServiceName() == null
                && cond.getNfInstanceId() == null) {
            throw new NfManagementService.NfManagementException(
                    400, "Requested SubscrCond is not supported yet");
        }

        /* The NF must not dictate the subscriptionId */
        subscriptionData.setSubscriptionId(null);
        subscriptionData.setNrfSupportedFeatures("0");

        Duration validity = Duration.ofSeconds(properties.getSubscriptionValidityDuration());
        SubscriptionContext ctx = subscriptionRegistry.add(subscriptionData, validity);
        ctx.getData().setValidityTime(
                OffsetDateTime.now().plus(validity).toString());

        String location = properties.getApiRoot()
                + "/nnrf-nfm/v1/subscriptions/" + ctx.getId();

        ResponseEntity<SubscriptionData> response = ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.LOCATION, location)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ctx.getData());

        /* Notify the subscriber about already-registered matching NFs */
        notificationService.notifyExistingMatches(ctx);

        return response;
    }

    /* ------------------------------------------------------------------ */
    /* NFStatusUpdate — PATCH /subscriptions/{subscriptionId}              */
    /* nrf_nnrf_handle_nf_status_update() in Open5GS                       */
    /* ------------------------------------------------------------------ */
    @PatchMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> nfStatusUpdate(@PathVariable String subscriptionId,
                                               @RequestBody(required = false) List<PatchItem> patchItems) {
        SubscriptionContext ctx = subscriptionRegistry.find(subscriptionId);
        if (ctx == null) {
            throw new NfManagementService.NfManagementException(
                    404, "Not found [" + subscriptionId + "]");
        }
        if (patchItems == null) {
            throw new NfManagementService.NfManagementException(400, "No PatchItemList");
        }

        String validityTime = null;
        for (PatchItem item : patchItems) {
            if (item.getOp() != com.lyra5gc.common.model.PatchOperation.REPLACE) {
                log.error("Unknown PatchItem.Operation [{}]", item.getOp());
                continue;
            }
            if (SbiConstants.PATCH_PATH_VALIDITY_TIME.equals(item.getPath())
                    && item.getValue() != null && item.getValue().isTextual()) {
                validityTime = item.getValue().asText();
            } else if (!SbiConstants.PATCH_PATH_VALIDITY_TIME.equals(item.getPath())) {
                log.error("Unknown PatchItem.Path [{}]", item.getPath());
            }
        }
        if (validityTime == null) {
            throw new NfManagementService.NfManagementException(400, "No validityTime");
        }

        OffsetDateTime time;
        try {
            time = OffsetDateTime.parse(validityTime);
        } catch (Exception e) {
            throw new NfManagementService.NfManagementException(
                    400, "parse error [" + validityTime + "]");
        }
        Duration validity = Duration.between(OffsetDateTime.now(), time);
        if (validity.isNegative()) {
            throw new NfManagementService.NfManagementException(
                    400, "invalid validity [" + validityTime + "]");
        }

        subscriptionRegistry.refreshValidity(ctx, validity);
        log.info("[{}] Subscription updated until {} [validity:{}s]",
                subscriptionId, validityTime, validity.toSeconds());

        return ResponseEntity.noContent().build();
    }

    /* ------------------------------------------------------------------ */
    /* NFStatusUnsubscribe — DELETE /subscriptions/{subscriptionId}        */
    /* ------------------------------------------------------------------ */
    @DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> nfStatusUnsubscribe(@PathVariable String subscriptionId) {
        if (!subscriptionRegistry.remove(subscriptionId)) {
            throw new NfManagementService.NfManagementException(
                    404, "Not found [" + subscriptionId + "]");
        }
        return ResponseEntity.noContent().build();
    }

    /* ------------------------------------------------------------------ */
    /* RFC 7807 error responses                                            */
    /* ------------------------------------------------------------------ */
    @ExceptionHandler(NfManagementService.NfManagementException.class)
    public ResponseEntity<ProblemDetails> handleError(NfManagementService.NfManagementException e) {
        log.warn("Nnrf_NFManagement error [{}]: {}", e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .contentType(PROBLEM_JSON)
                .body(new ProblemDetails(e.getHttpStatus(), e.getMessage()));
    }
}

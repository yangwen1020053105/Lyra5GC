package com.lyra5gc.nrf.controller;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.PlmnId;
import com.lyra5gc.common.model.ProblemDetails;
import com.lyra5gc.common.model.SearchResult;
import com.lyra5gc.common.model.Snssai;
import com.lyra5gc.common.sbi.DiscoveryOption;
import com.lyra5gc.common.sbi.SbiConstants;
import com.lyra5gc.common.sbi.SbiJson;
import com.lyra5gc.nrf.config.NrfProperties;
import com.lyra5gc.nrf.context.NfInstanceContext;
import com.lyra5gc.nrf.registry.NfRegistry;
import com.lyra5gc.nrf.service.NfDiscoveryService;
import com.lyra5gc.nrf.service.NfManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Nnrf_NFDiscovery service API — TS 29.510 clause 5.3.
 * Mirrors nrf_nnrf_handle_nf_discover() in Open5GS.
 */
@RestController
@RequestMapping("/nnrf-disc/v1")
public class NfDiscoveryController {

    private static final Logger log = LoggerFactory.getLogger(NfDiscoveryController.class);
    private static final MediaType PROBLEM_JSON = MediaType.valueOf(SbiConstants.CONTENT_PROBLEM);

    private final NfRegistry nfRegistry;
    private final NfDiscoveryService discoveryService;
    private final NrfProperties properties;

    public NfDiscoveryController(NfRegistry nfRegistry,
                                 NfDiscoveryService discoveryService,
                                 NrfProperties properties) {
        this.nfRegistry = nfRegistry;
        this.discoveryService = discoveryService;
        this.properties = properties;
    }

    @GetMapping("/nf-instances")
    public ResponseEntity<SearchResult> nfDiscover(@RequestParam Map<String, String> params) {

        String targetNfTypeStr = params.get(SbiConstants.QP_TARGET_NF_TYPE);
        if (targetNfTypeStr == null) {
            throw new NfManagementService.NfManagementException(400, "No target-nf-type");
        }
        NfType targetNfType = NfType.fromString(targetNfTypeStr);
        if (targetNfType == null) {
            throw new NfManagementService.NfManagementException(
                    400, "Invalid target-nf-type [" + targetNfTypeStr + "]");
        }

        String requesterNfTypeStr = params.get(SbiConstants.QP_REQUESTER_NF_TYPE);
        if (requesterNfTypeStr == null) {
            throw new NfManagementService.NfManagementException(400, "No requester-nf-type");
        }
        NfType requesterNfType = NfType.fromString(requesterNfTypeStr);
        if (requesterNfType == null) {
            throw new NfManagementService.NfManagementException(
                    400, "Invalid requester-nf-type [" + requesterNfTypeStr + "]");
        }

        log.debug("NF-Discover : Requester[{}] Target[{}]", requesterNfType, targetNfType);

        DiscoveryOption option = parseDiscoveryParams(params);

        Integer limit = null;
        if (params.get(SbiConstants.QP_LIMIT) != null) {
            try {
                limit = Integer.parseInt(params.get(SbiConstants.QP_LIMIT));
            } catch (NumberFormatException e) {
                throw new NfManagementService.NfManagementException(
                        400, "Invalid limit [" + params.get(SbiConstants.QP_LIMIT) + "]");
            }
        }

        /*
         * TS 33.518 4.2.2.2.1: if the requester registered with sNssais,
         * reject slice queries outside that set with 403 Forbidden.
         */
        if (!option.getSnssais().isEmpty() && option.getRequesterNfInstanceId() != null) {
            NfInstanceContext requester = nfRegistry.find(option.getRequesterNfInstanceId());
            if (requester != null
                    && !discoveryService.requesterAuthorizedForSnssais(requester, option)) {
                log.error("NF [{}] not authorized for requested S-NSSAI",
                        option.getRequesterNfInstanceId());
                throw new NfManagementService.NfManagementException(403,
                        "Requester not authorized for requested S-NSSAI");
            }
        }

        SearchResult result = new SearchResult();
        result.setValidityPeriod(properties.getNfInstanceValidityDuration());
        result.setNfInstances(new ArrayList<>());

        int i = 0;
        for (NfInstanceContext ctx : nfRegistry.list()) {
            if (ctx.getProfile().getNfType() != targetNfType) {
                continue;
            }
            if (!discoveryService.isAllowedNfType(ctx, requesterNfType)) {
                continue;
            }
            /* TS 29.510: without target-plmn-list, only same-PLMN NFs match */
            if (option.getTargetPlmnList().isEmpty()
                    && !discoveryService.servingPlmnMatched(ctx)) {
                continue;
            }
            if (!option.isEmpty()
                    && !discoveryService.isMatched(ctx, requesterNfType, option)) {
                continue;
            }
            if (limit != null && i >= limit) {
                break;
            }

            NFProfile profile = ctx.getProfile();
            log.info("[{}:{}] NF-Discovered [NF-Type:{},NF-Status:{}]", ctx.getId(), i,
                    profile.getNfType(), profile.getNfStatus());
            result.getNfInstances().add(profile);
            i++;
        }

        if (limit != null) {
            result.setNumOfNfInstComplete(i);
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=" + result.getValidityPeriod())
                .contentType(MediaType.APPLICATION_JSON)
                .body(result);
    }

    private DiscoveryOption parseDiscoveryParams(Map<String, String> params) {
        DiscoveryOption option = new DiscoveryOption();

        option.setTargetNfInstanceId(params.get(SbiConstants.QP_TARGET_NF_INSTANCE_ID));
        option.setRequesterNfInstanceId(params.get(SbiConstants.QP_REQUESTER_NF_INSTANCE_ID));

        String serviceNames = params.get(SbiConstants.QP_SERVICE_NAMES);
        if (serviceNames != null && !serviceNames.isBlank()) {
            option.setServiceNames(List.of(serviceNames.split(",")));
        }

        String snssais = params.get(SbiConstants.QP_SNSSAIS);
        if (snssais != null) {
            try {
                option.setSnssais(SbiJson.mapper().readerForListOf(Snssai.class)
                        .readValue(snssais));
            } catch (Exception e) {
                throw new NfManagementService.NfManagementException(
                        400, "Invalid snssais [" + snssais + "]");
            }
        }

        option.setDnn(params.get(SbiConstants.QP_DNN));

        String targetPlmnList = params.get(SbiConstants.QP_TARGET_PLMN_LIST);
        if (targetPlmnList != null) {
            try {
                option.setTargetPlmnList(SbiJson.mapper().readerForListOf(PlmnId.class)
                        .readValue(targetPlmnList));
            } catch (Exception e) {
                throw new NfManagementService.NfManagementException(
                        400, "Invalid target-plmn-list [" + targetPlmnList + "]");
            }
        }

        String requesterPlmnList = params.get(SbiConstants.QP_REQUESTER_PLMN_LIST);
        if (requesterPlmnList != null) {
            try {
                option.setRequesterPlmnList(SbiJson.mapper().readerForListOf(PlmnId.class)
                        .readValue(requesterPlmnList));
            } catch (Exception e) {
                throw new NfManagementService.NfManagementException(
                        400, "Invalid requester-plmn-list [" + requesterPlmnList + "]");
            }
        }

        option.setHnrfUri(params.get(SbiConstants.QP_HNRF_URI));

        String requesterFeatures = params.get(SbiConstants.QP_REQUESTER_FEATURES);
        if (requesterFeatures != null) {
            try {
                option.setRequesterFeatures(Long.parseUnsignedLong(requesterFeatures, 16));
            } catch (NumberFormatException e) {
                throw new NfManagementService.NfManagementException(
                        400, "Invalid requester-features [" + requesterFeatures + "]");
            }
        }

        return option;
    }

    @ExceptionHandler(NfManagementService.NfManagementException.class)
    public ResponseEntity<ProblemDetails> handleError(NfManagementService.NfManagementException e) {
        log.warn("Nnrf_NFDiscovery error [{}]: {}", e.getHttpStatus(), e.getMessage());
        return ResponseEntity.status(e.getHttpStatus())
                .contentType(PROBLEM_JSON)
                .body(new ProblemDetails(e.getHttpStatus(), e.getMessage()));
    }
}

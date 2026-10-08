package com.lyra5gc.common.sbi.client;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NfStatus;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.PatchItem;
import com.lyra5gc.common.model.PatchOperation;
import com.lyra5gc.common.model.SearchResult;
import com.lyra5gc.common.sbi.DiscoveryOption;
import com.lyra5gc.common.sbi.SbiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * Client side of Nnrf_NFManagement / Nnrf_NFDiscovery —
 * mirrors Open5GS lib/sbi/nnrf-build.c (register / update-heartbeat /
 * de-register / discover) used by every NF.
 */
public class NfRegistrationClient {

    private static final Logger log = LoggerFactory.getLogger(NfRegistrationClient.class);
    private static final MediaType PATCH_JSON = MediaType.valueOf(SbiConstants.CONTENT_PATCH);

    private final WebClient webClient;
    private final String nrfUri;
    private final NfType selfType;
    /** User-Agent NF type prefix, e.g. "AMF" (TS 29.500 clause 5.2.2.2). */
    private final String userAgent;

    public NfRegistrationClient(WebClient webClient, String nrfUri, NfType selfType) {
        this.webClient = webClient;
        this.nrfUri = nrfUri;
        this.selfType = selfType;
        this.userAgent = selfType.getValue() + "-lyra5gc";
    }

    private String nfmUrl() {
        return nrfUri + "/nnrf-nfm/" + SbiConstants.API_V1
                + "/" + SbiConstants.RESOURCE_NF_INSTANCES;
    }

    /**
     * NFRegister — PUT /nnrf-nfm/v1/nf-instances/{nfInstanceId}.
     *
     * @return the NFProfile returned by the NRF
     */
    public Mono<NFProfile> register(NFProfile profile) {
        return webClient.put()
                .uri(nfmUrl() + "/" + profile.getNfInstanceId())
                .contentType(MediaType.APPLICATION_JSON)
                .header(SbiConstants.HDR_USER_AGENT, userAgent)
                .bodyValue(profile)
                .retrieve()
                .bodyToMono(NFProfile.class)
                .doOnSuccess(p -> log.info("[{}] NF registered to NRF [{}]",
                        profile.getNfInstanceId(), nrfUri))
                .doOnError(e -> log.error("[{}] NFRegister failed: {}",
                        profile.getNfInstanceId(), e.getMessage()));
    }

    /**
     * HeartBeat / NFUpdate — PATCH with /nfStatus + /load
     * (ogs_nnrf_nfm_build_update()).
     */
    public Mono<Void> heartbeat(String nfInstanceId, int load) {
        PatchItem statusItem = new PatchItem();
        statusItem.setOp(PatchOperation.REPLACE);
        statusItem.setPath(SbiConstants.PATCH_PATH_NF_STATUS);
        statusItem.setValue(SbiJsonHolder.textNode(NfStatus.REGISTERED.getValue()));

        PatchItem loadItem = new PatchItem();
        loadItem.setOp(PatchOperation.REPLACE);
        loadItem.setPath(SbiConstants.PATCH_PATH_LOAD);
        loadItem.setValue(SbiJsonHolder.intNode(load));

        return webClient.patch()
                .uri(nfmUrl() + "/" + nfInstanceId)
                .contentType(PATCH_JSON)
                .header(SbiConstants.HDR_USER_AGENT, userAgent)
                .bodyValue(List.of(statusItem, loadItem))
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> log.debug("[{}] Heartbeat OK", nfInstanceId))
                .doOnError(e -> log.error("[{}] Heartbeat failed: {}",
                        nfInstanceId, e.getMessage()))
                .then();
    }

    /**
     * NFDeregister — DELETE /nnrf-nfm/v1/nf-instances/{nfInstanceId}.
     */
    public Mono<Void> deregister(String nfInstanceId) {
        return webClient.delete()
                .uri(nfmUrl() + "/" + nfInstanceId)
                .header(SbiConstants.HDR_USER_AGENT, userAgent)
                .retrieve()
                .toBodilessEntity()
                .doOnSuccess(r -> log.info("[{}] NF de-registered from NRF", nfInstanceId))
                .doOnError(e -> log.error("[{}] NFDeregister failed: {}",
                        nfInstanceId, e.getMessage()))
                .then();
    }

    /**
     * NFDiscover — GET /nnrf-disc/v1/nf-instances (ogs_nnrf_disc_build_discover()).
     */
    public Mono<SearchResult> discover(NfType targetNfType, DiscoveryOption option) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(nrfUri + "/nnrf-disc/" + SbiConstants.API_V1
                        + "/" + SbiConstants.RESOURCE_NF_INSTANCES);
        NfType requesterType = selfType;
        for (String[] param : option.toQueryParams(targetNfType, requesterType)) {
            builder.queryParam(param[0], param[1]);
        }
        URI uri = builder.encode().build().toUri();

        return webClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header(SbiConstants.HDR_USER_AGENT, userAgent)
                .retrieve()
                .bodyToMono(SearchResult.class);
    }

    /**
     * NFProfileRetrieval — GET /nnrf-nfm/v1/nf-instances/{nfInstanceId}.
     */
    public Mono<NFProfile> getNfProfile(String nfInstanceId) {
        return webClient.get()
                .uri(nfmUrl() + "/" + nfInstanceId)
                .accept(MediaType.APPLICATION_JSON)
                .header(SbiConstants.HDR_USER_AGENT, userAgent)
                .retrieve()
                .bodyToMono(NFProfile.class);
    }

    /**
     * NFListRetrieval — GET /nnrf-nfm/v1/nf-instances (3gppHal document).
     */
    public Mono<String> listNfInstances() {
        return webClient.get()
                .uri(nfmUrl())
                .header(SbiConstants.HDR_USER_AGENT, userAgent)
                .retrieve()
                .bodyToMono(String.class);
    }

    /** Jackson node helpers kept private to the client. */
    private static final class SbiJsonHolder {
        static com.fasterxml.jackson.databind.JsonNode textNode(String v) {
            return com.lyra5gc.common.sbi.SbiJson.mapper().getNodeFactory().textNode(v);
        }

        static com.fasterxml.jackson.databind.JsonNode intNode(int v) {
            return com.lyra5gc.common.sbi.SbiJson.mapper().getNodeFactory().numberNode(v);
        }
    }
}

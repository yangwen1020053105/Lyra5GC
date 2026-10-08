package com.lyra5gc.amf.controller;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.SearchResult;
import com.lyra5gc.common.sbi.DiscoveryOption;
import com.lyra5gc.common.sbi.client.NfRegistrationClient;
import com.lyra5gc.common.sbi.client.SelfNfRegistrar;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

/**
 * Test endpoints exercising the AMF -> NRF SBI calls
 * (Nnrf_NFManagement / Nnrf_NFDiscovery client side).
 */
@RestController
@RequestMapping("/test")
public class AmfTestController {

    private final NfRegistrationClient nrfClient;
    private final SelfNfRegistrar registrar;

    public AmfTestController(NfRegistrationClient nrfClient, SelfNfRegistrar registrar) {
        this.nrfClient = nrfClient;
        this.registrar = registrar;
    }

    /** AMF's own NF profile. */
    @GetMapping("/self")
    public NFProfile self() {
        return registrar.getProfile();
    }

    /**
     * Nnrf_NFDiscovery: discover NFs via the NRF.
     * e.g. GET /test/discover?targetNfType=AUSF&serviceNames=nausf-auth
     */
    @GetMapping("/discover")
    public Mono<ResponseEntity<SearchResult>> discover(
            @RequestParam NfType targetNfType,
            @RequestParam(required = false) String serviceNames,
            @RequestParam(required = false) String dnn) {
        DiscoveryOption option = new DiscoveryOption();
        if (serviceNames != null) {
            List<String> names = Arrays.stream(serviceNames.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList();
            option.setServiceNames(names);
        }
        option.setDnn(dnn);
        return nrfClient.discover(targetNfType, option)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(502).build()));
    }

    /** Nnrf_NFManagement NFListRetrieval (3gppHal document). */
    @GetMapping(value = "/nf-instances", produces = "application/3gppHal+json")
    public Mono<String> listNfInstances() {
        return nrfClient.listNfInstances();
    }

    /** Nnrf_NFManagement NFProfileRetrieval. */
    @GetMapping("/nf-instances/{nfInstanceId}")
    public Mono<ResponseEntity<NFProfile>> getNfProfile(@PathVariable String nfInstanceId) {
        return nrfClient.getNfProfile(nfInstanceId)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.notFound().build()));
    }

    /** Trigger one HeartBeat immediately. */
    @PostMapping("/heartbeat")
    public Mono<ResponseEntity<Void>> heartbeat() {
        return registrar.heartbeatNow()
                .thenReturn(ResponseEntity.noContent().<Void>build())
                .onErrorResume(e -> Mono.just(ResponseEntity.status(502).build()));
    }
}

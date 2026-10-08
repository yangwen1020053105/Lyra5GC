package com.lyra5gc.scp.proxy;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NFService;
import com.lyra5gc.common.sbi.ApiRoots;
import com.lyra5gc.common.sbi.DiscoveryOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Local cache of NF instances discovered via the NRF —
 * mirrors ogs_sbi_nf_instance_find_by_discovery_param() usage in Open5GS SCP.
 */
@Component
public class NfInstanceCache {

    private static final Logger log = LoggerFactory.getLogger(NfInstanceCache.class);

    private record Entry(NFProfile profile, Instant expiresAt) {
        boolean expired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final Map<String, Entry> byInstanceId = new ConcurrentHashMap<>();

    /**
     * Stores all profiles of a SearchResult with the given validity period.
     */
    public void store(List<NFProfile> profiles, int validityPeriodSec) {
        Instant expiresAt = Instant.now().plusSeconds(validityPeriodSec);
        for (NFProfile profile : profiles) {
            if (profile.getNfInstanceId() == null) {
                continue;
            }
            byInstanceId.put(profile.getNfInstanceId(), new Entry(profile, expiresAt));
            log.info("[{}] (NF-discover) NF cached [type:{}]",
                    profile.getNfInstanceId(), profile.getNfType());
        }
    }

    public NFProfile findByInstanceId(String nfInstanceId) {
        Entry entry = byInstanceId.get(nfInstanceId);
        if (entry == null) {
            return null;
        }
        if (entry.expired()) {
            byInstanceId.remove(nfInstanceId);
            return null;
        }
        return entry.profile();
    }

    /**
     * Picks a cached NF of the requested type exposing the requested service.
     */
    public NFProfile findByDiscoveryParam(com.lyra5gc.common.model.NfType targetNfType,
                                          String serviceName,
                                          DiscoveryOption option) {
        for (Entry entry : byInstanceId.values()) {
            if (entry.expired()) {
                continue;
            }
            NFProfile profile = entry.profile();
            if (profile.getNfType() != targetNfType) {
                continue;
            }
            if (option.getTargetNfInstanceId() != null
                    && !option.getTargetNfInstanceId().equals(profile.getNfInstanceId())) {
                continue;
            }
            if (profile.findService(serviceName) != null) {
                return profile;
            }
        }
        return null;
    }

    /**
     * Resolves the apiRoot of the given service instance of an NF.
     */
    public String apiRootOf(NFProfile profile, String serviceName) {
        NFService service = profile.findService(serviceName);
        if (service == null) {
            return null;
        }
        return ApiRoots.of(service, profile.getFqdn(), profile.getIpv4Addresses());
    }
}

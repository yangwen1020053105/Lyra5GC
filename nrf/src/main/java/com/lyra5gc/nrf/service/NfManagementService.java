package com.lyra5gc.nrf.service;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.PatchItem;
import com.lyra5gc.common.model.PatchOperation;
import com.lyra5gc.common.model.PlmnId;
import com.lyra5gc.common.sbi.SbiConstants;
import com.lyra5gc.common.sbi.SbiJson;
import com.lyra5gc.nrf.config.NrfProperties;
import com.lyra5gc.nrf.context.NfInstanceContext;
import com.lyra5gc.nrf.registry.NfRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Nnrf_NFManagement business logic —
 * mirrors Open5GS src/nrf/nnrf-handler.c (nf_register / nf_update).
 */
@Service
public class NfManagementService {

    private static final Logger log = LoggerFactory.getLogger(NfManagementService.class);

    private final NfRegistry registry;
    private final NrfProperties properties;

    public NfManagementService(NfRegistry registry, NrfProperties properties) {
        this.registry = registry;
        this.properties = properties;
    }

    public record RegisterResult(int httpStatus, NFProfile responseProfile, String location) {
    }

    public static class NfManagementException extends RuntimeException {
        private final int httpStatus;

        public NfManagementException(int httpStatus, String detail) {
            super(detail);
            this.httpStatus = httpStatus;
        }

        public int getHttpStatus() {
            return httpStatus;
        }
    }

    /**
     * NFRegister / NFUpdate (complete replacement).
     * See nrf_nnrf_handle_nf_register() in Open5GS.
     */
    public RegisterResult register(String pathNfInstanceId, NFProfile profile) {
        if (profile == null) {
            throw badRequest("No NFProfile");
        }
        if (profile.getNfInstanceId() == null) {
            throw badRequest("No NFProfile.NFInstanceId");
        }
        if (pathNfInstanceId != null && !pathNfInstanceId.equals(profile.getNfInstanceId())) {
            throw badRequest("Mismatched NFInstanceId");
        }
        if (profile.getNfType() == null) {
            throw badRequest("No NFProfile.NFType");
        }
        if (profile.getNfStatus() == null) {
            throw badRequest("No NFProfile.NFStatus");
        }

        /*
         * Reject non-positive heartBeatTimer before it reaches the timer
         * layer (mirrors the Open5GS guard in nrf_nnrf_handle_nf_register).
         */
        if (profile.getHeartBeatTimer() != null && profile.getHeartBeatTimer() <= 0) {
            throw badRequest("Invalid heartBeatTimer [" + profile.getHeartBeatTimer() + "]");
        }

        /* Validate the PLMN-ID against configured serving PLMN-IDs */
        if (profile.getPlmnList() != null && !properties.getServingPlmnList().isEmpty()) {
            boolean plmnValid = profile.getPlmnList().stream()
                    .anyMatch(properties.getServingPlmnList()::contains);
            if (!plmnValid) {
                throw badRequest("PLMN-ID not allowed");
            }
        }

        /* The NF must expose at least one usable endpoint */
        boolean usableEndpoint = profile.getFqdn() != null
                || (profile.getIpv4Addresses() != null && !profile.getIpv4Addresses().isEmpty())
                || (profile.getIpv6Addresses() != null && !profile.getIpv6Addresses().isEmpty());
        if (!usableEndpoint) {
            throw badRequest("NFProfile has no usable endpoint");
        }

        /*
         * TS 29.510 Table 6.1.6.2.2-1: if the NF profile carries no plmnList,
         * the PLMN of the NRF is assumed.
         */
        if (profile.getPlmnList() == null && !properties.getServingPlmnList().isEmpty()) {
            profile.setPlmnList(new ArrayList<>(properties.getServingPlmnList()));
        }

        /* NRF applies its configured heartbeat if the NF did not send one */
        boolean heartbeatDefaulted = profile.getHeartBeatTimer() == null;
        int heartbeatInterval = heartbeatDefaulted
                ? properties.getHeartbeatInterval()
                : profile.getHeartBeatTimer();
        if (heartbeatDefaulted && heartbeatInterval > 0) {
            profile.setHeartBeatTimer(heartbeatInterval);
        }

        NfInstanceContext ctx = new NfInstanceContext(profile.getNfInstanceId());
        ctx.setProfile(profile);
        ctx.setHeartbeatInterval(heartbeatInterval);

        boolean created = registry.register(ctx);

        /*
         * TS 29.510 Annex B: if the NF signaled nfProfileChangesSupportInd,
         * the NRF answers with only the changed attributes.
         */
        NFProfile responseProfile;
        if (Boolean.TRUE.equals(profile.getNfProfileChangesSupportInd())) {
            NFProfile changes = new NFProfile();
            changes.setNfInstanceId(profile.getNfInstanceId());
            changes.setNfType(profile.getNfType());
            changes.setNfStatus(profile.getNfStatus());
            if (heartbeatDefaulted) {
                changes.setHeartBeatTimer(profile.getHeartBeatTimer());
            }
            changes.setNfProfileChangesInd(true);
            responseProfile = changes;
        } else {
            responseProfile = profile;
        }

        String location = properties.getApiRoot()
                + "/nnrf-nfm/" + SbiConstants.API_V1
                + "/" + SbiConstants.RESOURCE_NF_INSTANCES
                + "/" + profile.getNfInstanceId();

        return new RegisterResult(created ? 201 : 200, responseProfile, location);
    }

    /**
     * NFUpdate (JSON Patch) / HeartBeat.
     * See nrf_nnrf_handle_nf_update() in Open5GS.
     *
     * @param patchItems parsed JSON Patch array; empty array = pure heartbeat
     */
    public void update(String nfInstanceId, List<PatchItem> patchItems) {
        NfInstanceContext ctx = registry.find(nfInstanceId);
        if (ctx == null) {
            throw new NfManagementException(404, "Not found [" + nfInstanceId + "]");
        }
        if (patchItems == null) {
            throw badRequest("No PatchItemList");
        }

        for (PatchItem item : patchItems) {
            if (item.getOp() != PatchOperation.REPLACE) {
                log.error("Unknown PatchItem.Operation [{}]", item.getOp());
                continue;
            }
            switch (item.getPath() == null ? "" : item.getPath()) {
                case SbiConstants.PATCH_PATH_NF_STATUS -> {
                    /* value is a plain string, e.g. "REGISTERED" */
                    if (item.getValue() != null && item.getValue().isTextual()) {
                        com.lyra5gc.common.model.NfStatus status =
                                com.lyra5gc.common.model.NfStatus.fromString(item.getValue().asText());
                        if (status != null) {
                            ctx.getProfile().setNfStatus(status);
                        }
                    }
                }
                case SbiConstants.PATCH_PATH_LOAD -> {
                    if (item.getValue() != null && item.getValue().isNumber()) {
                        ctx.getProfile().setLoad(item.getValue().intValue());
                    }
                }
                case SbiConstants.PATCH_PATH_PLMN_LIST -> applyPlmnListPatch(ctx, item);
                default -> log.error("Unknown PatchItem.Path [{}]", item.getPath());
            }
        }

        /* Every accepted PATCH restarts the no-heartbeat timer */
        registry.touch(nfInstanceId);
    }

    /**
     * Validates the new plmnList against the serving PLMNs; commits only if valid.
     */
    private void applyPlmnListPatch(NfInstanceContext ctx, PatchItem item) {
        if (item.getValue() == null || !item.getValue().isArray()) {
            throw badRequest("Invalid value for /plmnList");
        }
        List<PlmnId> newPlmnList;
        try {
            newPlmnList = SbiJson.mapper().readerForListOf(PlmnId.class)
                    .readValue(item.getValue());
        } catch (Exception e) {
            throw badRequest("Invalid value for /plmnList");
        }
        if (!properties.getServingPlmnList().isEmpty()) {
            boolean plmnValid = newPlmnList.stream()
                    .anyMatch(properties.getServingPlmnList()::contains);
            if (!plmnValid) {
                throw badRequest("PLMN-ID not allowed");
            }
        }
        ctx.getProfile().setPlmnList(newPlmnList);
    }

    /**
     * NFDeregister — Open5GS nrf_nf_state_registered CASE(DELETE).
     */
    public void deregister(String nfInstanceId) {
        if (!registry.remove(nfInstanceId)) {
            throw new NfManagementException(404, "Not found [" + nfInstanceId + "]");
        }
        log.info("[{}] NF de-registered", nfInstanceId);
    }

    private static NfManagementException badRequest(String detail) {
        return new NfManagementException(400, detail);
    }
}

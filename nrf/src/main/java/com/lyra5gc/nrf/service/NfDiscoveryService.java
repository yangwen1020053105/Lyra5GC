package com.lyra5gc.nrf.service;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NFService;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.PlmnId;
import com.lyra5gc.common.model.Snssai;
import com.lyra5gc.common.sbi.DiscoveryOption;
import com.lyra5gc.nrf.config.NrfProperties;
import com.lyra5gc.nrf.context.NfInstanceContext;
import com.lyra5gc.nrf.registry.NfRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Nnrf_NFDiscovery matching rules —
 * mirrors ogs_sbi_discovery_option_is_matched() and
 * ogs_sbi_nf_instance_is_allowed_nf_type() in Open5GS lib/sbi/context.c.
 */
@Service
public class NfDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(NfDiscoveryService.class);

    private final NrfProperties properties;

    public NfDiscoveryService(NrfProperties properties) {
        this.properties = properties;
    }

    /**
     * ogs_sbi_nf_instance_is_allowed_nf_type():
     * if the profile restricts allowedNfTypes, the requester must be listed.
     */
    public boolean isAllowedNfType(NfInstanceContext ctx, NfType requesterNfType) {
        List<NfType> allowed = ctx.getProfile().getAllowedNfTypes();
        return allowed == null || allowed.isEmpty() || allowed.contains(requesterNfType);
    }

    /**
     * ogs_sbi_discovery_param_serving_plmn_list_is_matched():
     * when no target-plmn-list is given, an NF from a different PLMN must not
     * be returned (provided both sides know their PLMN).
     */
    public boolean servingPlmnMatched(NfInstanceContext ctx) {
        List<PlmnId> serving = properties.getServingPlmnList();
        List<PlmnId> nfPlmns = ctx.getProfile().getPlmnList();
        if (serving.isEmpty() || nfPlmns == null || nfPlmns.isEmpty()) {
            return true;
        }
        return nfPlmns.stream().anyMatch(serving::contains);
    }

    public boolean isMatched(NfInstanceContext ctx, NfType requesterNfType, DiscoveryOption option) {
        NFProfile profile = ctx.getProfile();

        if (option.getTargetNfInstanceId() != null
                && !option.getTargetNfInstanceId().equals(ctx.getId())) {
            return false;
        }

        if (!option.getServiceNames().isEmpty() && !serviceNamesMatched(profile, requesterNfType, option)) {
            return false;
        }

        if (!option.getTargetPlmnList().isEmpty()) {
            List<PlmnId> nfPlmns = profile.getPlmnList();
            if (nfPlmns == null || nfPlmns.stream()
                    .noneMatch(p -> option.getTargetPlmnList().contains(p))) {
                return false;
            }
        }

        /*
         * TS 33.518 4.2.2.2.1: if the target NF registered allowedNssais,
         * at least one requested S-NSSAI must fall within that set.
         */
        if (profile.getAllowedNssais() != null && !profile.getAllowedNssais().isEmpty()
                && !option.getSnssais().isEmpty()) {
            boolean allowed = option.getSnssais().stream()
                    .anyMatch(s -> profile.getAllowedNssais().contains(s));
            if (!allowed) {
                return false;
            }
        }

        /*
         * S-NSSAI filter: at least one requested S-NSSAI must be served by
         * the NF (matches against the profile-level sNssais; per-NF-info
         * matching like Open5GS smfInfo/amfInfo is not modeled).
         */
        if (!option.getSnssais().isEmpty()) {
            List<Snssai> nfSnssais = profile.getSNssais();
            if (nfSnssais == null || nfSnssais.isEmpty()
                    || option.getSnssais().stream().noneMatch(nfSnssais::contains)) {
                return false;
            }
        }

        if (option.getDnn() != null) {
            log.debug("dnn [{}] filter: no per-NF dnn info modeled, ignoring", option.getDnn());
        }
        if (option.getTai() != null) {
            log.debug("tai filter: no per-NF tai info modeled, ignoring");
        }
        if (option.getGuami() != null) {
            log.debug("guami filter: no per-NF guami info modeled, ignoring");
        }

        return true;
    }

    /**
     * ogs_sbi_discovery_option_service_names_is_matched():
     * at least one registered NFService must carry a requested service name
     * and allow the requester NF type.
     */
    private boolean serviceNamesMatched(NFProfile profile, NfType requesterNfType,
                                        DiscoveryOption option) {
        if (profile.getNfServiceList() == null) {
            return false;
        }
        for (NFService service : profile.getNfServiceList()) {
            if (service.getServiceName() == null
                    || !option.getServiceNames().contains(service.getServiceName())) {
                continue;
            }
            List<NfType> allowed = service.getAllowedNfTypes();
            if (allowed == null || allowed.isEmpty() || allowed.contains(requesterNfType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TS 33.518 4.2.2.2.1 requester-side check: if the requester NF registered
     * with sNssais, it may only query slices within that set.
     */
    public boolean requesterAuthorizedForSnssais(NfInstanceContext requester, DiscoveryOption option) {
        List<Snssai> registered = requester.getProfile().getSNssais();
        if (registered == null || registered.isEmpty()) {
            return true;
        }
        return option.getSnssais().stream().anyMatch(registered::contains);
    }
}

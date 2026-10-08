package com.lyra5gc.common.sbi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lyra5gc.common.model.Guami;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.PlmnId;
import com.lyra5gc.common.model.Snssai;
import com.lyra5gc.common.model.Tai;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Parses 3gpp-Sbi-Discovery-* custom header values into a DiscoveryOption —
 * mirrors the header handling in Open5GS src/scp/sbi-path.c.
 *
 * Header values carrying complex types (snssais, tai, guami, plmn lists)
 * are JSON-encoded per TS 29.500 clause 6.10.3.2.
 */
public final class DiscoveryHeaderParser {

    private DiscoveryHeaderParser() {
    }

    /**
     * @param headers case-insensitive header map (e.g. lower-cased names)
     * @return parsed DiscoveryOption (never null)
     * @throws DiscoveryHeaderException on malformed values
     */
    public static DiscoveryOption parse(Map<String, String> headers) {
        DiscoveryOption option = new DiscoveryOption();

        String targetInstanceId = headers.get(SbiConstants.HDR_DISCOVERY_TARGET_NF_INSTANCE_ID.toLowerCase());
        if (targetInstanceId != null) {
            option.setTargetNfInstanceId(targetInstanceId);
        }

        String requesterInstanceId = headers.get(SbiConstants.HDR_DISCOVERY_REQUESTER_NF_INSTANCE_ID.toLowerCase());
        if (requesterInstanceId != null) {
            option.setRequesterNfInstanceId(requesterInstanceId);
        }

        String serviceNames = headers.get(SbiConstants.HDR_DISCOVERY_SERVICE_NAMES.toLowerCase());
        if (serviceNames != null && !serviceNames.isBlank()) {
            option.setServiceNames(Arrays.stream(serviceNames.split(","))
                    .map(String::trim).filter(s -> !s.isEmpty()).toList());
        }

        String snssais = headers.get(SbiConstants.HDR_DISCOVERY_SNSSAIS.toLowerCase());
        if (snssais != null) {
            option.setSnssais(parseJsonList(snssais, new TypeReference<List<Snssai>>() {
            }, "snssais"));
        }

        String dnn = headers.get(SbiConstants.HDR_DISCOVERY_DNN.toLowerCase());
        if (dnn != null) {
            option.setDnn(dnn);
        }

        String tai = headers.get(SbiConstants.HDR_DISCOVERY_TAI.toLowerCase());
        if (tai != null) {
            option.setTai(parseJson(tai, Tai.class, "tai"));
        }

        String guami = headers.get(SbiConstants.HDR_DISCOVERY_GUAMI.toLowerCase());
        if (guami != null) {
            option.setGuami(parseJson(guami, Guami.class, "guami"));
        }

        String targetPlmnList = headers.get(SbiConstants.HDR_DISCOVERY_TARGET_PLMN_LIST.toLowerCase());
        if (targetPlmnList != null) {
            option.setTargetPlmnList(parseJsonList(targetPlmnList, new TypeReference<List<PlmnId>>() {
            }, "target-plmn-list"));
        }

        String requesterPlmnList = headers.get(SbiConstants.HDR_DISCOVERY_REQUESTER_PLMN_LIST.toLowerCase());
        if (requesterPlmnList != null) {
            option.setRequesterPlmnList(parseJsonList(requesterPlmnList, new TypeReference<List<PlmnId>>() {
            }, "requester-plmn-list"));
        }

        String hnrfUri = headers.get(SbiConstants.HDR_DISCOVERY_HNRF_URI.toLowerCase());
        if (hnrfUri != null) {
            option.setHnrfUri(hnrfUri);
        }

        String requesterFeatures = headers.get(SbiConstants.HDR_DISCOVERY_REQUESTER_FEATURES.toLowerCase());
        if (requesterFeatures != null) {
            try {
                option.setRequesterFeatures(Long.parseUnsignedLong(requesterFeatures, 16));
            } catch (NumberFormatException e) {
                throw new DiscoveryHeaderException("Invalid requester-features: " + requesterFeatures);
            }
        }

        return option;
    }

    public static NfType targetNfType(Map<String, String> headers) {
        String v = headers.get(SbiConstants.HDR_DISCOVERY_TARGET_NF_TYPE.toLowerCase());
        return v == null ? null : NfType.fromString(v);
    }

    /**
     * Extracts the requester NF type from the User-Agent header
     * (TS 29.500 clause 5.2.2.2: value starts with the NF type, e.g. "UDM-...").
     */
    public static NfType requesterNfTypeFromUserAgent(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        int dash = userAgent.indexOf('-');
        String v = dash < 0 ? userAgent.trim() : userAgent.substring(0, dash).trim();
        return NfType.fromString(v);
    }

    private static <T> T parseJson(String value, Class<T> type, String name) {
        try {
            return SbiJson.mapper().readValue(value, type);
        } catch (Exception e) {
            throw new DiscoveryHeaderException("Invalid " + name + " [" + value + "]");
        }
    }

    private static <T> T parseJsonList(String value, TypeReference<T> type, String name) {
        try {
            return SbiJson.mapper().readValue(value, type);
        } catch (Exception e) {
            throw new DiscoveryHeaderException("Invalid " + name + " [" + value + "]");
        }
    }

    public static class DiscoveryHeaderException extends RuntimeException {
        public DiscoveryHeaderException(String message) {
            super(message);
        }
    }
}

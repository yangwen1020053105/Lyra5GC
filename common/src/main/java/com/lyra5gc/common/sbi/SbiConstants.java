package com.lyra5gc.common.sbi;

/**
 * SBI protocol constants — 3GPP TS 29.500 / TS 29.510
 * (mirrors Open5GS lib/sbi/ogs-sbi.h).
 */
public final class SbiConstants {

    /* ---- Service API / resource names ---- */
    public static final String API_V1 = "v1";
    public static final String RESOURCE_NF_INSTANCES = "nf-instances";
    public static final String RESOURCE_SUBSCRIPTIONS = "subscriptions";

    /* ---- Content types ---- */
    public static final String CONTENT_JSON = "application/json";
    public static final String CONTENT_PATCH = "application/json-patch+json";
    public static final String CONTENT_PROBLEM = "application/problem+json";
    public static final String CONTENT_3GPP_HAL = "application/3gppHal+json";

    /* ---- Standard headers ---- */
    public static final String HDR_USER_AGENT = "User-Agent";

    /* ---- 3GPP custom headers (TS 29.500 clause 5.2.3.x) ---- */
    public static final String HDR_TARGET_APIROOT = "3gpp-Sbi-Target-apiRoot";
    public static final String HDR_CALLBACK = "3gpp-Sbi-Callback";
    public static final String HDR_NRF_URI = "3gpp-Sbi-Nrf-Uri";
    public static final String HDR_PRODUCER_ID = "3gpp-Sbi-Producer-Id";

    public static final String HDR_DISCOVERY_PREFIX = "3gpp-Sbi-Discovery-";
    public static final String HDR_DISCOVERY_TARGET_NF_TYPE = "3gpp-Sbi-Discovery-target-nf-type";
    public static final String HDR_DISCOVERY_REQUESTER_NF_TYPE = "3gpp-Sbi-Discovery-requester-nf-type";
    public static final String HDR_DISCOVERY_TARGET_NF_INSTANCE_ID = "3gpp-Sbi-Discovery-target-nf-instance-id";
    public static final String HDR_DISCOVERY_REQUESTER_NF_INSTANCE_ID = "3gpp-Sbi-Discovery-requester-nf-instance-id";
    public static final String HDR_DISCOVERY_SERVICE_NAMES = "3gpp-Sbi-Discovery-service-names";
    public static final String HDR_DISCOVERY_SNSSAIS = "3gpp-Sbi-Discovery-snssais";
    public static final String HDR_DISCOVERY_DNN = "3gpp-Sbi-Discovery-dnn";
    public static final String HDR_DISCOVERY_TAI = "3gpp-Sbi-Discovery-tai";
    public static final String HDR_DISCOVERY_GUAMI = "3gpp-Sbi-Discovery-guami";
    public static final String HDR_DISCOVERY_TARGET_PLMN_LIST = "3gpp-Sbi-Discovery-target-plmn-list";
    public static final String HDR_DISCOVERY_REQUESTER_PLMN_LIST = "3gpp-Sbi-Discovery-requester-plmn-list";
    public static final String HDR_DISCOVERY_HNRF_URI = "3gpp-Sbi-Discovery-hnrf-uri";
    public static final String HDR_DISCOVERY_REQUESTER_FEATURES = "3gpp-Sbi-Discovery-requester-features";

    /* ---- Callback names ---- */
    public static final String CALLBACK_NNRF_NFM_NF_STATUS_NOTIFY =
            "Nnrf_NFManagement_NFStatusNotify";

    /* ---- JSON Patch paths (TS 29.510) ---- */
    public static final String PATCH_PATH_NF_STATUS = "/nfStatus";
    public static final String PATCH_PATH_LOAD = "/load";
    public static final String PATCH_PATH_PLMN_LIST = "/plmnList";
    public static final String PATCH_PATH_VALIDITY_TIME = "/validityTime";

    /* ---- Nnrf_NFDiscovery query parameters ---- */
    public static final String QP_TARGET_NF_TYPE = "target-nf-type";
    public static final String QP_REQUESTER_NF_TYPE = "requester-nf-type";
    public static final String QP_TARGET_NF_INSTANCE_ID = "target-nf-instance-id";
    public static final String QP_REQUESTER_NF_INSTANCE_ID = "requester-nf-instance-id";
    public static final String QP_SERVICE_NAMES = "service-names";
    public static final String QP_SNSSAIS = "snssais";
    public static final String QP_DNN = "dnn";
    public static final String QP_TAI = "tai";
    public static final String QP_GUAMI = "guami";
    public static final String QP_TARGET_PLMN_LIST = "target-plmn-list";
    public static final String QP_REQUESTER_PLMN_LIST = "requester-plmn-list";
    public static final String QP_HNRF_URI = "hnrf-uri";
    public static final String QP_REQUESTER_FEATURES = "requester-features";
    public static final String QP_LIMIT = "limit";
    public static final String QP_NF_TYPE = "nf-type";

    private SbiConstants() {
    }
}

package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SubscrCond — TS 29.510 Table 6.1.6.2.6-1.
 * OneOf: nfType | serviceName | nfInstanceId.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscrCond {

    private NfType nfType;
    private String serviceName;
    private String nfInstanceId;

    public NfType getNfType() {
        return nfType;
    }

    public void setNfType(NfType nfType) {
        this.nfType = nfType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNfInstanceId() {
        return nfInstanceId;
    }

    public void setNfInstanceId(String nfInstanceId) {
        this.nfInstanceId = nfInstanceId;
    }
}

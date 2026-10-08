package com.lyra5gc.common.sbi;

import com.lyra5gc.common.model.Guami;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.PlmnId;
import com.lyra5gc.common.model.Snssai;
import com.lyra5gc.common.model.Tai;

import java.util.ArrayList;
import java.util.List;

/**
 * NF Discovery factors — mirrors ogs_sbi_discovery_option_t
 * (Open5GS lib/sbi/context.h).
 */
public class DiscoveryOption {

    private String targetNfInstanceId;
    private String requesterNfInstanceId;
    private List<String> serviceNames = new ArrayList<>();
    private List<Snssai> snssais = new ArrayList<>();
    private String dnn;
    private Tai tai;
    private Guami guami;
    private List<PlmnId> targetPlmnList = new ArrayList<>();
    private List<PlmnId> requesterPlmnList = new ArrayList<>();
    private String hnrfUri;
    private Long requesterFeatures;

    public String getTargetNfInstanceId() {
        return targetNfInstanceId;
    }

    public void setTargetNfInstanceId(String targetNfInstanceId) {
        this.targetNfInstanceId = targetNfInstanceId;
    }

    public String getRequesterNfInstanceId() {
        return requesterNfInstanceId;
    }

    public void setRequesterNfInstanceId(String requesterNfInstanceId) {
        this.requesterNfInstanceId = requesterNfInstanceId;
    }

    public List<String> getServiceNames() {
        return serviceNames;
    }

    public void setServiceNames(List<String> serviceNames) {
        this.serviceNames = serviceNames != null ? serviceNames : new ArrayList<>();
    }

    public List<Snssai> getSnssais() {
        return snssais;
    }

    public void setSnssais(List<Snssai> snssais) {
        this.snssais = snssais != null ? snssais : new ArrayList<>();
    }

    public String getDnn() {
        return dnn;
    }

    public void setDnn(String dnn) {
        this.dnn = dnn;
    }

    public Tai getTai() {
        return tai;
    }

    public void setTai(Tai tai) {
        this.tai = tai;
    }

    public Guami getGuami() {
        return guami;
    }

    public void setGuami(Guami guami) {
        this.guami = guami;
    }

    public List<PlmnId> getTargetPlmnList() {
        return targetPlmnList;
    }

    public void setTargetPlmnList(List<PlmnId> targetPlmnList) {
        this.targetPlmnList = targetPlmnList != null ? targetPlmnList : new ArrayList<>();
    }

    public List<PlmnId> getRequesterPlmnList() {
        return requesterPlmnList;
    }

    public void setRequesterPlmnList(List<PlmnId> requesterPlmnList) {
        this.requesterPlmnList = requesterPlmnList != null ? requesterPlmnList : new ArrayList<>();
    }

    public String getHnrfUri() {
        return hnrfUri;
    }

    public void setHnrfUri(String hnrfUri) {
        this.hnrfUri = hnrfUri;
    }

    public Long getRequesterFeatures() {
        return requesterFeatures;
    }

    public void setRequesterFeatures(Long requesterFeatures) {
        this.requesterFeatures = requesterFeatures;
    }

    public boolean isEmpty() {
        return targetNfInstanceId == null && requesterNfInstanceId == null
                && serviceNames.isEmpty() && snssais.isEmpty() && dnn == null
                && tai == null && guami == null && targetPlmnList.isEmpty()
                && requesterPlmnList.isEmpty() && hnrfUri == null
                && requesterFeatures == null;
    }

    /**
     * Encodes as Nnrf_NFDiscovery query parameters (TS 29.510 Table 6.2.3.2.3.1-1).
     */
    public List<String[]> toQueryParams(NfType targetNfType, NfType requesterNfType) {
        List<String[]> params = new ArrayList<>();
        params.add(new String[]{SbiConstants.QP_TARGET_NF_TYPE, targetNfType.getValue()});
        params.add(new String[]{SbiConstants.QP_REQUESTER_NF_TYPE, requesterNfType.getValue()});
        if (targetNfInstanceId != null) {
            params.add(new String[]{SbiConstants.QP_TARGET_NF_INSTANCE_ID, targetNfInstanceId});
        }
        if (requesterNfInstanceId != null) {
            params.add(new String[]{SbiConstants.QP_REQUESTER_NF_INSTANCE_ID, requesterNfInstanceId});
        }
        if (!serviceNames.isEmpty()) {
            params.add(new String[]{SbiConstants.QP_SERVICE_NAMES, String.join(",", serviceNames)});
        }
        if (!snssais.isEmpty()) {
            params.add(new String[]{SbiConstants.QP_SNSSAIS, SbiJson.write(snssais)});
        }
        if (dnn != null) {
            params.add(new String[]{SbiConstants.QP_DNN, dnn});
        }
        if (tai != null) {
            params.add(new String[]{SbiConstants.QP_TAI, SbiJson.write(tai)});
        }
        if (guami != null) {
            params.add(new String[]{SbiConstants.QP_GUAMI, SbiJson.write(guami)});
        }
        if (!targetPlmnList.isEmpty()) {
            params.add(new String[]{SbiConstants.QP_TARGET_PLMN_LIST, SbiJson.write(targetPlmnList)});
        }
        if (!requesterPlmnList.isEmpty()) {
            params.add(new String[]{SbiConstants.QP_REQUESTER_PLMN_LIST, SbiJson.write(requesterPlmnList)});
        }
        if (hnrfUri != null) {
            params.add(new String[]{SbiConstants.QP_HNRF_URI, hnrfUri});
        }
        if (requesterFeatures != null) {
            params.add(new String[]{SbiConstants.QP_REQUESTER_FEATURES,
                    Long.toHexString(requesterFeatures)});
        }
        return params;
    }
}

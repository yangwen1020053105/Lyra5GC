package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * NFProfile — TS 29.510 Table 6.1.6.2.2-1 (subset, aligned with Open5GS).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NFProfile {

    private String nfInstanceId;
    private String nfInstanceName;
    private NfType nfType;
    private NfStatus nfStatus;
    private Integer heartBeatTimer;
    private List<PlmnId> plmnList;
    private List<Snssai> sNssais;
    private List<String> nsiList;
    private String fqdn;
    private List<String> ipv4Addresses;
    private List<String> ipv6Addresses;
    private List<NfType> allowedNfTypes;
    private List<Snssai> allowedNssais;
    private Integer capacity;
    private Integer load;
    private Integer priority;
    private String recoveryTime;
    private String nrfSupportedFeatures;
    private List<NFService> nfServiceList;
    private List<DefaultNotificationSubscription> defaultNotificationSubscriptions;
    private Boolean nfProfileChangesSupportInd;
    private Boolean nfProfileChangesInd;

    public String getNfInstanceId() {
        return nfInstanceId;
    }

    public void setNfInstanceId(String nfInstanceId) {
        this.nfInstanceId = nfInstanceId;
    }

    public String getNfInstanceName() {
        return nfInstanceName;
    }

    public void setNfInstanceName(String nfInstanceName) {
        this.nfInstanceName = nfInstanceName;
    }

    public NfType getNfType() {
        return nfType;
    }

    public void setNfType(NfType nfType) {
        this.nfType = nfType;
    }

    public NfStatus getNfStatus() {
        return nfStatus;
    }

    public void setNfStatus(NfStatus nfStatus) {
        this.nfStatus = nfStatus;
    }

    public Integer getHeartBeatTimer() {
        return heartBeatTimer;
    }

    public void setHeartBeatTimer(Integer heartBeatTimer) {
        this.heartBeatTimer = heartBeatTimer;
    }

    public List<PlmnId> getPlmnList() {
        return plmnList;
    }

    public void setPlmnList(List<PlmnId> plmnList) {
        this.plmnList = plmnList;
    }

    public List<Snssai> getSNssais() {
        return sNssais;
    }

    public void setSNssais(List<Snssai> sNssais) {
        this.sNssais = sNssais;
    }

    public List<String> getNsiList() {
        return nsiList;
    }

    public void setNsiList(List<String> nsiList) {
        this.nsiList = nsiList;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public List<String> getIpv4Addresses() {
        return ipv4Addresses;
    }

    public void setIpv4Addresses(List<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
    }

    public List<String> getIpv6Addresses() {
        return ipv6Addresses;
    }

    public void setIpv6Addresses(List<String> ipv6Addresses) {
        this.ipv6Addresses = ipv6Addresses;
    }

    public List<NfType> getAllowedNfTypes() {
        return allowedNfTypes;
    }

    public void setAllowedNfTypes(List<NfType> allowedNfTypes) {
        this.allowedNfTypes = allowedNfTypes;
    }

    public List<Snssai> getAllowedNssais() {
        return allowedNssais;
    }

    public void setAllowedNssais(List<Snssai> allowedNssais) {
        this.allowedNssais = allowedNssais;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Integer getLoad() {
        return load;
    }

    public void setLoad(Integer load) {
        this.load = load;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getRecoveryTime() {
        return recoveryTime;
    }

    public void setRecoveryTime(String recoveryTime) {
        this.recoveryTime = recoveryTime;
    }

    public String getNrfSupportedFeatures() {
        return nrfSupportedFeatures;
    }

    public void setNrfSupportedFeatures(String nrfSupportedFeatures) {
        this.nrfSupportedFeatures = nrfSupportedFeatures;
    }

    public List<NFService> getNfServiceList() {
        return nfServiceList;
    }

    public void setNfServiceList(List<NFService> nfServiceList) {
        this.nfServiceList = nfServiceList;
    }

    public List<DefaultNotificationSubscription> getDefaultNotificationSubscriptions() {
        return defaultNotificationSubscriptions;
    }

    public void setDefaultNotificationSubscriptions(List<DefaultNotificationSubscription> subscriptions) {
        this.defaultNotificationSubscriptions = subscriptions;
    }

    public Boolean getNfProfileChangesSupportInd() {
        return nfProfileChangesSupportInd;
    }

    public void setNfProfileChangesSupportInd(Boolean nfProfileChangesSupportInd) {
        this.nfProfileChangesSupportInd = nfProfileChangesSupportInd;
    }

    public Boolean getNfProfileChangesInd() {
        return nfProfileChangesInd;
    }

    public void setNfProfileChangesInd(Boolean nfProfileChangesInd) {
        this.nfProfileChangesInd = nfProfileChangesInd;
    }

    /**
     * Returns the first usable IPv4 endpoint of the given service, or null.
     */
    public NFService findService(String serviceName) {
        if (nfServiceList == null || serviceName == null) {
            return null;
        }
        for (NFService s : nfServiceList) {
            if (serviceName.equals(s.getServiceName())) {
                return s;
            }
        }
        return null;
    }
}

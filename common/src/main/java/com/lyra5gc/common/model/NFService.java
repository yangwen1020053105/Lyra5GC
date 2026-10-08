package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * NFService — TS 29.510 Table 6.1.6.2.3-1 (subset, aligned with Open5GS).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NFService {

    private String serviceInstanceId;
    private String serviceName;
    private List<NFServiceVersion> versions;
    private UriScheme scheme;
    private NfServiceStatus nfServiceStatus;
    private String fqdn;
    private List<IpEndPoint> ipEndPoints;
    private String apiPrefix;
    private List<DefaultNotificationSubscription> defaultNotificationSubscriptions;
    private List<NfType> allowedNfTypes;
    private Integer priority;
    private Integer capacity;
    private Integer load;
    private String supportedFeatures;

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<NFServiceVersion> getVersions() {
        return versions;
    }

    public void setVersions(List<NFServiceVersion> versions) {
        this.versions = versions;
    }

    public UriScheme getScheme() {
        return scheme;
    }

    public void setScheme(UriScheme scheme) {
        this.scheme = scheme;
    }

    public NfServiceStatus getNfServiceStatus() {
        return nfServiceStatus;
    }

    public void setNfServiceStatus(NfServiceStatus nfServiceStatus) {
        this.nfServiceStatus = nfServiceStatus;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdn) {
        this.fqdn = fqdn;
    }

    public List<IpEndPoint> getIpEndPoints() {
        return ipEndPoints;
    }

    public void setIpEndPoints(List<IpEndPoint> ipEndPoints) {
        this.ipEndPoints = ipEndPoints;
    }

    public String getApiPrefix() {
        return apiPrefix;
    }

    public void setApiPrefix(String apiPrefix) {
        this.apiPrefix = apiPrefix;
    }

    public List<DefaultNotificationSubscription> getDefaultNotificationSubscriptions() {
        return defaultNotificationSubscriptions;
    }

    public void setDefaultNotificationSubscriptions(List<DefaultNotificationSubscription> subscriptions) {
        this.defaultNotificationSubscriptions = subscriptions;
    }

    public List<NfType> getAllowedNfTypes() {
        return allowedNfTypes;
    }

    public void setAllowedNfTypes(List<NfType> allowedNfTypes) {
        this.allowedNfTypes = allowedNfTypes;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
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

    public String getSupportedFeatures() {
        return supportedFeatures;
    }

    public void setSupportedFeatures(String supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }
}

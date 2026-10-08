package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SubscriptionData — TS 29.510 Table 6.1.6.2.4-1 (subset).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionData {

    private String nfStatusNotificationUri;
    private NfType reqNfType;
    private String reqNfInstanceId;
    private SubscrCond subscrCond;
    private String subscriptionId;
    private String validityTime;
    private java.util.List<NotificationEventType> reqNotifEvents;
    private String requesterFeatures;
    private String nrfSupportedFeatures;

    public String getNfStatusNotificationUri() {
        return nfStatusNotificationUri;
    }

    public void setNfStatusNotificationUri(String nfStatusNotificationUri) {
        this.nfStatusNotificationUri = nfStatusNotificationUri;
    }

    public NfType getReqNfType() {
        return reqNfType;
    }

    public void setReqNfType(NfType reqNfType) {
        this.reqNfType = reqNfType;
    }

    public String getReqNfInstanceId() {
        return reqNfInstanceId;
    }

    public void setReqNfInstanceId(String reqNfInstanceId) {
        this.reqNfInstanceId = reqNfInstanceId;
    }

    public SubscrCond getSubscrCond() {
        return subscrCond;
    }

    public void setSubscrCond(SubscrCond subscrCond) {
        this.subscrCond = subscrCond;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getValidityTime() {
        return validityTime;
    }

    public void setValidityTime(String validityTime) {
        this.validityTime = validityTime;
    }

    public java.util.List<NotificationEventType> getReqNotifEvents() {
        return reqNotifEvents;
    }

    public void setReqNotifEvents(java.util.List<NotificationEventType> reqNotifEvents) {
        this.reqNotifEvents = reqNotifEvents;
    }

    public String getRequesterFeatures() {
        return requesterFeatures;
    }

    public void setRequesterFeatures(String requesterFeatures) {
        this.requesterFeatures = requesterFeatures;
    }

    public String getNrfSupportedFeatures() {
        return nrfSupportedFeatures;
    }

    public void setNrfSupportedFeatures(String nrfSupportedFeatures) {
        this.nrfSupportedFeatures = nrfSupportedFeatures;
    }
}

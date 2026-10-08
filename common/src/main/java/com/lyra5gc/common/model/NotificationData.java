package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * NotificationData — TS 29.510 Table 6.1.6.2.5-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationData {

    private NotificationEventType event;
    private String nfInstanceUri;
    private NFProfile nfProfile;
    private String profileChanges;

    public NotificationEventType getEvent() {
        return event;
    }

    public void setEvent(NotificationEventType event) {
        this.event = event;
    }

    public String getNfInstanceUri() {
        return nfInstanceUri;
    }

    public void setNfInstanceUri(String nfInstanceUri) {
        this.nfInstanceUri = nfInstanceUri;
    }

    public NFProfile getNfProfile() {
        return nfProfile;
    }

    public void setNfProfile(NFProfile nfProfile) {
        this.nfProfile = nfProfile;
    }

    public String getProfileChanges() {
        return profileChanges;
    }

    public void setProfileChanges(String profileChanges) {
        this.profileChanges = profileChanges;
    }
}

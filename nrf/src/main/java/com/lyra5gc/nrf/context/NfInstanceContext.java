package com.lyra5gc.nrf.context;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NfStatus;

import java.util.concurrent.ScheduledFuture;

/**
 * Server-side state of a registered NF instance
 * (ogs_sbi_nf_instance_t in Open5GS).
 */
public class NfInstanceContext {

    private final String id;
    private volatile NFProfile profile;
    /** Heartbeat interval in seconds; 0 disables the no-heartbeat timer. */
    private volatile int heartbeatInterval;
    private volatile ScheduledFuture<?> noHeartbeatTimer;

    public NfInstanceContext(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public NFProfile getProfile() {
        return profile;
    }

    public void setProfile(NFProfile profile) {
        this.profile = profile;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public ScheduledFuture<?> getNoHeartbeatTimer() {
        return noHeartbeatTimer;
    }

    public void setNoHeartbeatTimer(ScheduledFuture<?> noHeartbeatTimer) {
        this.noHeartbeatTimer = noHeartbeatTimer;
    }

    public NfStatus getStatus() {
        NFProfile p = profile;
        return p != null ? p.getNfStatus() : null;
    }
}

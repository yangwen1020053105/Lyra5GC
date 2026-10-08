package com.lyra5gc.nrf.config;

import com.lyra5gc.common.model.PlmnId;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * NRF configuration — mirrors the nrf section of open5gs configs.
 */
@ConfigurationProperties(prefix = "nrf")
public class NrfProperties {

    /**
     * This NRF's own apiRoot, used to build Location and nfInstanceUri.
     */
    private String apiRoot = "http://127.0.0.1:7777";

    /**
     * Serving PLMN IDs of this NRF. Registrations carrying a plmnList that
     * does not intersect are rejected (Open5GS nrf_nnrf_handle_nf_register).
     */
    private List<PlmnId> servingPlmnList = new ArrayList<>();

    /**
     * Default heartBeatTimer (seconds) applied when the NF did not provide one.
     */
    private int heartbeatInterval = 10;

    /**
     * Extra margin (seconds) added to the heartbeat interval before the NRF
     * declares the NF dead (ogs_local_conf()->time.nf_instance.no_heartbeat_margin).
     * Must be > 0 so a heartbeat arriving exactly at the interval boundary
     * is still accepted.
     */
    private int noHeartbeatMargin = 1;

    /**
     * Subscription validity duration (seconds) unless refreshed by PATCH.
     */
    private long subscriptionValidityDuration = 86400;

    /**
     * validityPeriod reported in Nnrf_NFDiscovery SearchResult (seconds).
     */
    private int nfInstanceValidityDuration = 3600;

    public String getApiRoot() {
        return apiRoot;
    }

    public void setApiRoot(String apiRoot) {
        this.apiRoot = apiRoot;
    }

    public List<PlmnId> getServingPlmnList() {
        return servingPlmnList;
    }

    public void setServingPlmnList(List<PlmnId> servingPlmnList) {
        this.servingPlmnList = servingPlmnList;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public int getNoHeartbeatMargin() {
        return noHeartbeatMargin;
    }

    public void setNoHeartbeatMargin(int noHeartbeatMargin) {
        this.noHeartbeatMargin = noHeartbeatMargin;
    }

    public long getSubscriptionValidityDuration() {
        return subscriptionValidityDuration;
    }

    public void setSubscriptionValidityDuration(long subscriptionValidityDuration) {
        this.subscriptionValidityDuration = subscriptionValidityDuration;
    }

    public int getNfInstanceValidityDuration() {
        return nfInstanceValidityDuration;
    }

    public void setNfInstanceValidityDuration(int nfInstanceValidityDuration) {
        this.nfInstanceValidityDuration = nfInstanceValidityDuration;
    }
}

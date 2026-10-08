package com.lyra5gc.ausf.config;

import com.lyra5gc.common.model.PlmnId;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * AUSF configuration.
 */
@ConfigurationProperties(prefix = "ausf")
public class AusfProperties {

    /** NRF apiRoot used for registration/heartbeat. */
    private String nrfUri = "http://127.0.0.1:7777";

    /** This AUSF's NFInstanceId; generated once if not configured. */
    private String nfInstanceId = UUID.randomUUID().toString();

    /** Address advertised to other NFs via the NRF. */
    private String host = "127.0.0.1";
    private int port = 8082;

    /** heartBeatTimer reported to the NRF (seconds). */
    private int heartbeatInterval = 10;

    /** Serving PLMN(s) advertised in the NF profile. */
    private List<PlmnId> plmnList = new ArrayList<>(List.of(new PlmnId("001", "01")));

    public String getNrfUri() {
        return nrfUri;
    }

    public void setNrfUri(String nrfUri) {
        this.nrfUri = nrfUri;
    }

    public String getNfInstanceId() {
        return nfInstanceId;
    }

    public void setNfInstanceId(String nfInstanceId) {
        this.nfInstanceId = nfInstanceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(int heartbeatInterval) {
        this.heartbeatInterval = heartbeatInterval;
    }

    public List<PlmnId> getPlmnList() {
        return plmnList;
    }

    public void setPlmnList(List<PlmnId> plmnList) {
        this.plmnList = plmnList;
    }
}

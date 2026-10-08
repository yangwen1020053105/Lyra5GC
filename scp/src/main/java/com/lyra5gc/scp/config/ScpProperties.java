package com.lyra5gc.scp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SCP configuration.
 */
@ConfigurationProperties(prefix = "scp")
public class ScpProperties {

    /**
     * apiRoot of the NRF used for delegated discovery,
     * e.g. http://127.0.0.1:7777
     */
    private String nrfUri = "http://127.0.0.1:7777";

    public String getNrfUri() {
        return nrfUri;
    }

    public void setNrfUri(String nrfUri) {
        this.nrfUri = nrfUri;
    }
}

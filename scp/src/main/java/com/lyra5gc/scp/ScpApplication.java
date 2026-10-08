package com.lyra5gc.scp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Service Communication Proxy — 3GPP TS 23.501 (indirect communication,
 * delegated discovery) / TS 29.500 clause 6.10.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ScpApplication {

    public static void main(String[] args) {
        SpringApplication.run(ScpApplication.class, args);
    }
}

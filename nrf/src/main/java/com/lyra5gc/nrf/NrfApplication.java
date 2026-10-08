package com.lyra5gc.nrf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Network Repository Function — 3GPP TS 29.510.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class NrfApplication {

    public static void main(String[] args) {
        SpringApplication.run(NrfApplication.class, args);
    }
}

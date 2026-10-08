package com.lyra5gc.amf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Access and Mobility Management Function.
 * Registers itself to the NRF (Nnrf_NFManagement) and sends heartbeats.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AmfApplication {

    public static void main(String[] args) {
        SpringApplication.run(AmfApplication.class, args);
    }
}

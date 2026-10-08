package com.lyra5gc.ausf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Authentication Server Function.
 * Registers itself to the NRF (Nnrf_NFManagement) and sends heartbeats.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class AusfApplication {

    public static void main(String[] args) {
        SpringApplication.run(AusfApplication.class, args);
    }
}

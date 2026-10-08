package com.lyra5gc.scp.config;

import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

/**
 * SBI transport configuration (same rationale as the NRF side).
 */
@Configuration
public class ScpTransportConfig {

    @Bean
    public NettyServerCustomizer h2cServerCustomizer() {
        return server -> server.protocol(HttpProtocol.H2C, HttpProtocol.HTTP11);
    }

    @Bean
    public WebClient sbiWebClient() {
        HttpClient httpClient = HttpClient.create().protocol(HttpProtocol.H2C);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}

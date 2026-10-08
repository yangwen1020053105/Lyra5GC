package com.lyra5gc.nrf.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * SBI transport configuration.
 *
 * Server: accepts both HTTP/2 cleartext (h2c, prior-knowledge) and HTTP/1.1
 * — 3GPP TS 29.500 mandates HTTP/2; HTTP/1.1 is tolerated for debugging.
 *
 * Client (notifications): h2c prior-knowledge, as used between 5GC NFs.
 */
@Configuration
public class SbiTransportConfig {

    @Bean
    public NettyServerCustomizer h2cServerCustomizer() {
        return server -> server
                .protocol(HttpProtocol.H2C, HttpProtocol.HTTP11)
                .option(ChannelOption.SO_BACKLOG, 1024);
    }

    @Bean
    public WebClient sbiWebClient() {
        HttpClient httpClient = HttpClient.create().protocol(HttpProtocol.H2C);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService nrfScheduler() {
        return Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "nrf-timer");
            t.setDaemon(true);
            return t;
        });
    }
}

package com.lyra5gc.ausf.config;

import com.lyra5gc.common.model.IpEndPoint;
import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NFService;
import com.lyra5gc.common.model.NFServiceVersion;
import com.lyra5gc.common.model.NfServiceStatus;
import com.lyra5gc.common.model.NfStatus;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.ServiceNames;
import com.lyra5gc.common.model.TransportProtocol;
import com.lyra5gc.common.model.UriScheme;
import com.lyra5gc.common.sbi.client.NfRegistrationClient;
import com.lyra5gc.common.sbi.client.SelfNfRegistrar;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

import java.util.List;

/**
 * AUSF SBI wiring: h2c transport, NRF client, self registration.
 */
@Configuration
public class AusfConfig {

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

    @Bean
    public NfRegistrationClient nfRegistrationClient(WebClient sbiWebClient,
                                                     AusfProperties properties) {
        return new NfRegistrationClient(sbiWebClient, properties.getNrfUri(), NfType.AUSF);
    }

    @Bean
    public NFProfile ausfProfile(AusfProperties properties) {
        NFProfile profile = new NFProfile();
        profile.setNfInstanceId(properties.getNfInstanceId());
        profile.setNfInstanceName("lyra5gc-ausf");
        profile.setNfType(NfType.AUSF);
        profile.setNfStatus(NfStatus.REGISTERED);
        profile.setHeartBeatTimer(properties.getHeartbeatInterval());
        profile.setPlmnList(properties.getPlmnList());
        profile.setIpv4Addresses(List.of(properties.getHost()));
        profile.setLoad(0);
        profile.setCapacity(100);
        profile.setPriority(1);

        IpEndPoint endPoint = new IpEndPoint();
        endPoint.setIpv4Address(properties.getHost());
        endPoint.setPort(properties.getPort());
        endPoint.setTransport(TransportProtocol.TCP);

        List<NFService> services = List.of(
                ServiceNames.NAUSF_AUTH,
                ServiceNames.NAUSF_SORPROTECTION,
                ServiceNames.NAUSF_UPUPROTECTION
        ).stream().map(name -> {
            NFService service = new NFService();
            service.setServiceInstanceId(name + "-" + properties.getNfInstanceId());
            service.setServiceName(name);
            service.setScheme(UriScheme.HTTP);
            service.setNfServiceStatus(NfServiceStatus.REGISTERED);
            service.setVersions(List.of(new NFServiceVersion("v1", "1.0.0")));
            service.setIpEndPoints(List.of(endPoint));
            return service;
        }).toList();
        profile.setNfServiceList(services);

        return profile;
    }

    @Bean
    public SelfNfRegistrar selfNfRegistrar(NfRegistrationClient client, NFProfile ausfProfile) {
        return new SelfNfRegistrar(client, ausfProfile);
    }
}

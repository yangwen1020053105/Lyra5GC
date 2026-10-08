package com.lyra5gc.amf.config;

import com.lyra5gc.common.model.IpEndPoint;
import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NFService;
import com.lyra5gc.common.model.NFServiceVersion;
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
 * AMF SBI wiring: h2c transport, NRF client, self registration.
 */
@Configuration
public class AmfConfig {

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
                                                     AmfProperties properties) {
        return new NfRegistrationClient(sbiWebClient, properties.getNrfUri(), NfType.AMF);
    }

    @Bean
    public NFProfile amfProfile(AmfProperties properties) {
        NFProfile profile = new NFProfile();
        profile.setNfInstanceId(properties.getNfInstanceId());
        profile.setNfInstanceName("lyra5gc-amf");
        profile.setNfType(NfType.AMF);
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
                ServiceNames.NAMF_COMM,
                ServiceNames.NAMF_EVTS,
                ServiceNames.NAMF_MT,
                ServiceNames.NAMF_LOC
        ).stream().map(name -> {
            NFService service = new NFService();
            service.setServiceInstanceId(name + "-" + properties.getNfInstanceId());
            service.setServiceName(name);
            service.setScheme(UriScheme.HTTP);
            service.setNfServiceStatus(
                    com.lyra5gc.common.model.NfServiceStatus.REGISTERED);
            service.setVersions(List.of(new NFServiceVersion("v1", "1.0.0")));
            service.setIpEndPoints(List.of(endPoint));
            return service;
        }).toList();
        profile.setNfServiceList(services);

        return profile;
    }

    @Bean
    public SelfNfRegistrar selfNfRegistrar(NfRegistrationClient client, NFProfile amfProfile) {
        return new SelfNfRegistrar(client, amfProfile);
    }
}

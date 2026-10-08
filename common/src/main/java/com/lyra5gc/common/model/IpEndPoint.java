package com.lyra5gc.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * IpEndPoint — TS 29.510 Table 6.1.6.2.9-1.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IpEndPoint {

    private String ipv4Address;
    private String ipv6Address;
    private TransportProtocol transport;
    private Integer port;

    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public TransportProtocol getTransport() {
        return transport;
    }

    public void setTransport(TransportProtocol transport) {
        this.transport = transport;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}

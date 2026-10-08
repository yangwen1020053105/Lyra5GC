package com.lyra5gc.common.sbi;

import com.lyra5gc.common.model.IpEndPoint;
import com.lyra5gc.common.model.NFService;
import com.lyra5gc.common.model.UriScheme;

/**
 * Builds the apiRoot (scheme://host[:port][/apiPrefix]) of an NF service,
 * mirroring ogs_sbi_client_apiroot() in Open5GS.
 */
public final class ApiRoots {

    private ApiRoots() {
    }

    /**
     * Derives the apiRoot for an NF service.
     * Falls back to the NF-level FQDN / addresses when the service carries none.
     */
    public static String of(NFService service, String nfFqdn, java.util.List<String> nfIpv4) {
        UriScheme scheme = service.getScheme() != null ? service.getScheme() : UriScheme.HTTP;

        String host = null;
        Integer port = null;

        if (service.getIpEndPoints() != null) {
            for (IpEndPoint ep : service.getIpEndPoints()) {
                if (ep.getIpv4Address() != null) {
                    host = ep.getIpv4Address();
                    port = ep.getPort();
                    break;
                }
                if (ep.getIpv6Address() != null) {
                    host = "[" + ep.getIpv6Address() + "]";
                    port = ep.getPort();
                    break;
                }
            }
        }
        if (host == null && service.getFqdn() != null) {
            host = service.getFqdn();
        }
        if (host == null && nfFqdn != null) {
            host = nfFqdn;
        }
        if (host == null && nfIpv4 != null && !nfIpv4.isEmpty()) {
            host = nfIpv4.get(0);
        }
        if (host == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(scheme.getValue()).append("://").append(host);
        if (port != null) {
            sb.append(':').append(port);
        }
        if (service.getApiPrefix() != null) {
            sb.append(service.getApiPrefix());
        }
        return sb.toString();
    }

    /**
     * Derives the apiRoot of an NF from its profile-level addressing.
     */
    public static String ofProfile(com.lyra5gc.common.model.NFProfile profile) {
        String host = null;
        if (profile.getIpv4Addresses() != null && !profile.getIpv4Addresses().isEmpty()) {
            host = profile.getIpv4Addresses().get(0);
        } else if (profile.getFqdn() != null) {
            host = profile.getFqdn();
        } else if (profile.getIpv6Addresses() != null && !profile.getIpv6Addresses().isEmpty()) {
            host = "[" + profile.getIpv6Addresses().get(0) + "]";
        }
        return host == null ? null : "http://" + host;
    }
}

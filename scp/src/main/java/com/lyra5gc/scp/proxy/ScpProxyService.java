package com.lyra5gc.scp.proxy;

import com.lyra5gc.common.model.NFProfile;
import com.lyra5gc.common.model.NfType;
import com.lyra5gc.common.model.ProblemDetails;
import com.lyra5gc.common.model.SearchResult;
import com.lyra5gc.common.sbi.DiscoveryHeaderParser;
import com.lyra5gc.common.sbi.DiscoveryOption;
import com.lyra5gc.common.sbi.SbiConstants;
import com.lyra5gc.scp.config.ScpProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SCP proxy core — mirrors request_handler() / send_discover() /
 * send_request() of Open5GS src/scp/sbi-path.c.
 *
 * Flow (delegated discovery, TS 29.500 clause 6.10.3):
 *   1. 3gpp-Sbi-Target-apiRoot present      -> forward there directly
 *   2. cached/discovered NF matches         -> forward to it
 *   3. discovery headers present            -> Nnrf_NFDiscovery, then forward
 *   4. otherwise                            -> 404 (no route)
 */
@Service
public class ScpProxyService {

    private static final Logger log = LoggerFactory.getLogger(ScpProxyService.class);
    private static final MediaType PROBLEM_JSON = MediaType.valueOf(SbiConstants.CONTENT_PROBLEM);
    private static final Pattern NNRF_DISC_PATTERN =
            Pattern.compile("nnrf-disc\\s*=\\s*\"([^\"]+)\"");

    private final WebClient sbiWebClient;
    private final NfInstanceCache nfCache;
    private final ScpProperties properties;

    public ScpProxyService(WebClient sbiWebClient, NfInstanceCache nfCache,
                           ScpProperties properties) {
        this.sbiWebClient = sbiWebClient;
        this.nfCache = nfCache;
        this.properties = properties;
    }

    public Mono<ResponseEntity<byte[]>> handle(HttpMethod method, String path, String rawQuery,
                                               Map<String, String> headers, byte[] body) {
        /* ---- Extract 3GPP custom headers (mirrors request_handler) ---- */
        NfType requesterNfType =
                DiscoveryHeaderParser.requesterNfTypeFromUserAgent(
                        headers.get(SbiConstants.HDR_USER_AGENT.toLowerCase()));
        if (requesterNfType == null) {
            log.error("[{}] No User-Agent", path);
            return problem(400, "No User-Agent");
        }

        String targetApiRoot = headers.get(SbiConstants.HDR_TARGET_APIROOT.toLowerCase());
        NfType targetNfType = DiscoveryHeaderParser.targetNfType(headers);

        DiscoveryOption discoveryOption;
        try {
            discoveryOption = DiscoveryHeaderParser.parse(headers);
        } catch (DiscoveryHeaderParser.DiscoveryHeaderException e) {
            return problem(400, e.getMessage());
        }

        String serviceName = discoveryOption.getServiceNames().isEmpty()
                ? null : discoveryOption.getServiceNames().get(0);

        boolean discoveryPresence = targetNfType != null || serviceName != null;
        if (discoveryPresence && (targetNfType == null || serviceName == null)) {
            log.error("[{}] No Mandatory Discovery [{}:{}]", path, targetNfType, serviceName);
            return problem(400, "No Mandatory Discovery");
        }

        /* ---- Direct forwarding via 3gpp-Sbi-Target-apiRoot ---- */
        if (targetApiRoot != null) {
            return forward(method, targetApiRoot, path, rawQuery, headers, body, null);
        }

        /* ---- Target resolution ---- */
        NFProfile target = null;
        if (discoveryPresence) {
            if (targetNfType == NfType.NRF) {
                /* Requests targeting the NRF go to the configured NRF */
                return forward(method, properties.getNrfUri(), path, rawQuery, headers, body, null);
            }
            if (discoveryOption.getTargetNfInstanceId() != null) {
                target = nfCache.findByInstanceId(discoveryOption.getTargetNfInstanceId());
            }
            if (target == null) {
                target = nfCache.findByDiscoveryParam(targetNfType, serviceName, discoveryOption);
            }
        }

        if (target != null) {
            String apiRoot = nfCache.apiRootOf(target, serviceName);
            if (apiRoot == null) {
                return problem(504, "(NF discover) No client [" + serviceName + "]");
            }
            return forward(method, apiRoot, path, rawQuery, headers, body,
                    target.getNfInstanceId());
        }

        /* ---- Delegated discovery via the NRF ---- */
        if (discoveryPresence) {
            String nrfUri = nrfUriFromHeader(headers.get(SbiConstants.HDR_NRF_URI.toLowerCase()));
            if (nrfUri == null) {
                nrfUri = properties.getNrfUri();
            }
            if (nrfUri == null) {
                log.error("No NRF");
                return problem(500, "No NRF");
            }

            /*
             * TS 29.500 6.10.3.2 NOTE 3: the SCP assumes the service request
             * corresponds to the first service name in the header.
             */
            DiscoveryOption queryOption = discoveryOption;
            if (discoveryOption.getServiceNames().size() > 1) {
                queryOption = discoveryOption;
                queryOption.setServiceNames(List.of(serviceName));
            }

            return discover(nrfUri, targetNfType, requesterNfType, queryOption)
                    .flatMap(searchResult -> {
                        nfCache.store(searchResult.getNfInstances(),
                                searchResult.getValidityPeriod() != null
                                        ? searchResult.getValidityPeriod() : 3600);

                        NFProfile discovered = nfCache.findByDiscoveryParam(
                                targetNfType, serviceName, discoveryOption);
                        if (discovered == null) {
                            log.error("(NF discover) No NF-Instance [{}:{}]",
                                    targetNfType, serviceName);
                            return problem(504, "(NF discover) No NF-Instance ["
                                    + targetNfType + ":" + serviceName + "]");
                        }
                        String apiRoot = nfCache.apiRootOf(discovered, serviceName);
                        if (apiRoot == null) {
                            return problem(504, "(NF discover) No client ["
                                    + serviceName + "]");
                        }
                        return forward(method, apiRoot, path, rawQuery, headers, body,
                                discovered.getNfInstanceId());
                    })
                    .onErrorResume(e -> {
                        log.error("NF-Discover failed: {}", e.getMessage());
                        return problem(500, "NF-Discover failed: " + e.getMessage());
                    });
        }

        /* No discovery info, no target: nothing to proxy */
        return problem(404, "No route for [" + path + "]");
    }

    /**
     * Nnrf_NFDiscovery GET request to the NRF — ogs_nnrf_disc_build_discover().
     */
    private Mono<SearchResult> discover(String nrfUri, NfType targetNfType,
                                        NfType requesterNfType, DiscoveryOption option) {
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString(nrfUri + "/nnrf-disc/" + SbiConstants.API_V1
                        + "/" + SbiConstants.RESOURCE_NF_INSTANCES);
        for (String[] param : option.toQueryParams(targetNfType, requesterNfType)) {
            builder.queryParam(param[0], param[1]);
        }
        URI uri = builder.encode().build().toUri();

        log.debug("Delegated discovery -> {}", uri);

        return sbiWebClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON)
                .header(SbiConstants.HDR_USER_AGENT, "SCP")
                .retrieve()
                .bodyToMono(SearchResult.class);
    }

    /**
     * Forwards the original request to the target apiRoot —
     * send_request() + copy_request() in Open5GS.
     */
    private Mono<ResponseEntity<byte[]>> forward(HttpMethod method, String apiRoot,
                                                 String path, String rawQuery,
                                                 Map<String, String> headers, byte[] body,
                                                 String producerId) {
        String uri = apiRoot + path + (rawQuery != null && !rawQuery.isEmpty()
                ? "?" + rawQuery : "");

        log.debug("Forwarding {} {}", method, uri);

        WebClient.RequestBodySpec spec = sbiWebClient.method(method)
                .uri(uri)
                .headers(h -> copyRequestHeaders(headers, h));

        WebClient.RequestHeadersSpec<?> requestSpec =
                (body != null && body.length > 0)
                        ? spec.bodyValue(body)
                        : spec;

        return requestSpec.exchangeToMono(response ->
                        response.bodyToMono(byte[].class)
                                .defaultIfEmpty(new byte[0])
                                .map(responseBody -> {
                                    HttpHeaders responseHeaders = new HttpHeaders();
                                    response.headers().asHttpHeaders().forEach((name, values) -> {
                                        if (!isHopByHop(name)) {
                                            responseHeaders.put(name, values);
                                        }
                                    });
                                    /* TS 29.500: identify the NF service producer */
                                    if (producerId != null) {
                                        responseHeaders.set(SbiConstants.HDR_PRODUCER_ID, producerId);
                                    }
                                    return ResponseEntity.status(response.statusCode())
                                            .headers(responseHeaders)
                                            .body(responseBody);
                                }))
                .onErrorResume(e -> {
                    log.error("send_request() failed: {}", e.getMessage());
                    return problem(500, "send_request() failed: " + e.getMessage());
                });
    }

    /**
     * copy_request(): strips the discovery custom headers and target-apiRoot;
     * :scheme/:authority are replaced by the HTTP client.
     */
    private void copyRequestHeaders(Map<String, String> source, HttpHeaders target) {
        source.forEach((name, value) -> {
            String lower = name.toLowerCase();
            if (lower.startsWith(SbiConstants.HDR_DISCOVERY_PREFIX.toLowerCase())
                    || lower.equals(SbiConstants.HDR_TARGET_APIROOT.toLowerCase())
                    || isHopByHop(lower)) {
                return;
            }
            target.add(name, value);
        });
    }

    private static boolean isHopByHop(String name) {
        String lower = name.toLowerCase();
        return lower.equals("host")
                || lower.equals("content-length")
                || lower.equals("transfer-encoding")
                || lower.equals("connection");
    }

    /**
     * Parses the 3gpp-Sbi-Nrf-Uri header
     * (e.g. {@code nnrf-nfm="http://..."; nnrf-disc="http://..."}).
     */
    private static String nrfUriFromHeader(String nrfUriHeader) {
        if (nrfUriHeader == null) {
            return null;
        }
        Matcher m = NNRF_DISC_PATTERN.matcher(nrfUriHeader);
        return m.find() ? m.group(1) : null;
    }

    private static Mono<ResponseEntity<byte[]>> problem(int status, String detail) {
        byte[] body = SbiJsonBytes.of(new ProblemDetails(status, detail));
        return Mono.just(ResponseEntity.status(status)
                .contentType(PROBLEM_JSON)
                .body(body));
    }

    /** Serialization helper kept separate to keep the proxy flow readable. */
    private static final class SbiJsonBytes {
        static byte[] of(Object value) {
            return com.lyra5gc.common.sbi.SbiJson.write(value).getBytes(
                    java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}

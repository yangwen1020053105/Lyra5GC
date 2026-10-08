package com.lyra5gc.scp.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Catch-all SBI entry point of the SCP — every inbound request is either
 * proxied (direct or after delegated discovery) like Open5GS request_handler().
 */
@RestController
public class ScpProxyController {

    private static final Logger log = LoggerFactory.getLogger(ScpProxyController.class);

    private final ScpProxyService proxyService;

    public ScpProxyController(ScpProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping("/**")
    public Mono<ResponseEntity<byte[]>> proxy(ServerHttpRequest request) {
        /* Lower-cased header view (HTTP field names are case-insensitive) */
        Map<String, String> headers = new HashMap<>();
        request.getHeaders().forEach((name, values) -> {
            if (!values.isEmpty()) {
                headers.put(name.toLowerCase(), values.get(0));
            }
        });

        String path = request.getPath().pathWithinApplication().value();
        String rawQuery = request.getURI().getRawQuery();

        return DataBufferUtils.join(request.getBody())
                .map(buffer -> {
                    byte[] body = new byte[buffer.readableByteCount()];
                    buffer.read(body);
                    DataBufferUtils.release(buffer);
                    return body;
                })
                .defaultIfEmpty(new byte[0])
                .flatMap(body -> proxyService.handle(
                        request.getMethod(), path, rawQuery, headers, body));
    }
}

package com.example.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Cross-cutting request logging applied to every route. Tags each request
 * with a correlation id (propagated to downstream services via header) and
 * logs method, path, response status and latency.
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Instant start = Instant.now();
        String correlationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        String finalCorrelationId = correlationId;

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r.header(CORRELATION_ID_HEADER, finalCorrelationId))
                .build();

        log.info("[{}] --> {} {}", correlationId,
                exchange.getRequest().getMethod(), exchange.getRequest().getURI());

        return chain.filter(mutatedExchange).then(Mono.fromRunnable(() -> {
            Duration elapsed = Duration.between(start, Instant.now());
            log.info("[{}] <-- status={} elapsedMs={}", finalCorrelationId,
                    mutatedExchange.getResponse().getStatusCode(), elapsed.toMillis());
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

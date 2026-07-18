package com.example.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Key resolvers for Redis-based distributed rate limiting.
 * These beans determine how to identify clients for rate limit tracking.
 */
@Configuration
public class RateLimitKeyResolverConfig {

    /**
     * Rate limit by client IP address.
     * Use this for anonymous/unauthenticated endpoints or when you want
     * to prevent abuse from specific IP addresses.
     */
    @Bean
    public KeyResolver remoteAddrKeyResolver() {
        return exchange -> {
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            String remoteAddr = remoteAddress != null && remoteAddress.getAddress() != null
                    ? remoteAddress.getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(remoteAddr);
        };
    }

    /**
     * Rate limit by authenticated user (principal name from JWT).
     * Use this for authenticated endpoints where you want to apply
     * per-user rate limits (e.g., API quotas).
     * 
     * Falls back to IP address if no principal is found.
     */
    @Bean
    public KeyResolver principalNameKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> principal.getName())
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    var remoteAddress = exchange.getRequest().getRemoteAddress();
                    return remoteAddress != null && remoteAddress.getAddress() != null
                            ? remoteAddress.getAddress().getHostAddress()
                            : "unknown";
                }));
    }

    /**
     * Rate limit by custom header (e.g., API key).
     * Use this when clients are identified by API keys in headers.
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> {
            String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-Key");
            return Mono.just(apiKey != null && !apiKey.isBlank() ? apiKey : "anonymous");
        };
    }

    /**
     * Combined resolver: Rate limit by user if authenticated, otherwise by IP.
     * This provides the most comprehensive rate limiting strategy.
     */
    @Primary
    @Bean
    public KeyResolver combinedKeyResolver() {
        return exchange -> exchange.getPrincipal()
                .map(principal -> "user:" + principal.getName())
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    var remoteAddress = exchange.getRequest().getRemoteAddress();
                    String addr = remoteAddress != null && remoteAddress.getAddress() != null
                            ? remoteAddress.getAddress().getHostAddress()
                            : "unknown";
                    return "ip:" + addr;
                }));
    }
}

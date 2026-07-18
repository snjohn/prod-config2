package com.example.gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Custom health indicator that checks Redis connectivity.
 * Verifies that the Redis server used for session storage is reachable.
 * 
 * This appears in the /actuator/health endpoint as "redis" status.
 * Note: Spring Boot already provides a RedisHealthIndicator, but this
 * custom implementation adds session-specific context.
 */
@Component
public class RedisSessionHealthIndicator implements ReactiveHealthIndicator {

    private final ReactiveRedisConnectionFactory connectionFactory;

    public RedisSessionHealthIndicator(ReactiveRedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    @Override
    public Mono<Health> health() {
        return connectionFactory.getReactiveConnection()
                .serverCommands()
                .info("server")
                .map(info -> {
                    String version = extractVersion(info.getProperty("redis_version"));
                    return Health.up()
                            .withDetail("version", version)
                            .withDetail("purpose", "Session storage")
                            .withDetail("status", "Redis is reachable")
                            .build();
                })
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(ex -> Mono.just(
                    Health.down()
                            .withDetail("error", ex.getClass().getSimpleName())
                            .withDetail("message", ex.getMessage())
                            .withDetail("purpose", "Session storage")
                            .build()
                ))
                .defaultIfEmpty(Health.unknown()
                        .withDetail("status", "Unable to determine Redis health")
                        .build());
    }

    private String extractVersion(String versionProperty) {
        return versionProperty != null ? versionProperty : "unknown";
    }
}

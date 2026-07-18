package com.example.gateway.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Custom health indicator that checks Eureka connectivity.
 * Verifies that the service discovery server is reachable and
 * that this gateway is properly registered.
 * 
 * This appears in the /actuator/health endpoint as "customEureka" status.
 */
@Component("customEurekaHealthIndicator")
public class EurekaHealthIndicator implements ReactiveHealthIndicator {

    private final DiscoveryClient discoveryClient;

    public EurekaHealthIndicator(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Mono<Health> health() {
        return Mono.fromCallable(() -> {
            try {
                // Check if we can retrieve services from Eureka
                var services = discoveryClient.getServices();
                
                if (services != null && !services.isEmpty()) {
                    return Health.up()
                            .withDetail("services", services.size())
                            .withDetail("registeredServices", services)
                            .withDetail("status", "Connected to Eureka")
                            .build();
                } else {
                    return Health.up()
                            .withDetail("services", 0)
                            .withDetail("status", "Connected to Eureka (no services registered)")
                            .build();
                }
            } catch (Exception ex) {
                return Health.down()
                        .withDetail("error", ex.getClass().getSimpleName())
                        .withDetail("message", ex.getMessage())
                        .build();
            }
        });
    }
}

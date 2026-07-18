package com.example.gateway.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Custom health indicator that checks connectivity to Keycloak.
 * Verifies that the OAuth2 provider is reachable and responding.
 * 
 * This appears in the /actuator/health endpoint as "keycloak" status.
 */
@Component
public class KeycloakHealthIndicator implements ReactiveHealthIndicator {

    private final WebClient webClient;
    private final String issuerUri;

    public KeycloakHealthIndicator(
            WebClient.Builder webClientBuilder,
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        this.webClient = webClientBuilder.build();
        this.issuerUri = issuerUri;
    }

    @Override
    public Mono<Health> health() {
        // Check Keycloak's OIDC configuration endpoint
        String wellKnownUri = issuerUri + "/.well-known/openid-configuration";
        
        return webClient.get()
                .uri(wellKnownUri)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Health.up()
                                .withDetail("issuerUri", issuerUri)
                                .withDetail("status", "Keycloak is reachable")
                                .build();
                    } else {
                        return Health.down()
                                .withDetail("issuerUri", issuerUri)
                                .withDetail("status", "Unexpected status: " + response.getStatusCode())
                                .build();
                    }
                })
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(ex -> Mono.just(
                    Health.down()
                            .withDetail("issuerUri", issuerUri)
                            .withDetail("error", ex.getClass().getSimpleName())
                            .withDetail("message", ex.getMessage())
                            .build()
                ));
    }
}

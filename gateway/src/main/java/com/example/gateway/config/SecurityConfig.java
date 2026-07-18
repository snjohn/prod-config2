package com.example.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;

/**
 * Centralizes authentication at the gateway so internal services do not each
 * need to validate credentials. Requests are authenticated here via a JWT
 * bearer token (OAuth2 resource server); public routes (e.g. health checks,
 * login) are explicitly permitted.
 * <p>
 * Configure the issuer in application.yml:
 * {@code spring.security.oauth2.resourceserver.jwt.issuer-uri}
 * <p>
 * The whole filter chain can be disabled with
 * {@code patternslib.gateway.security.enabled=false} (e.g. for local
 * development, or when authentication is instead handled by an upstream
 * load balancer / service mesh) — in that case a permit-all chain is used
 * instead so the application doesn't fall back to Spring Security's default
 * generated-password lockdown.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain jwtSecurityWebFilterChain(
            ServerHttpSecurity http,
            ServerLogoutSuccessHandler oidcLogoutSuccessHandler,
            ServerAuthenticationEntryPoint authenticationEntryPoint) {
        http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .pathMatchers("/actuator/**").hasRole("ADMIN")
                .pathMatchers("/public/**").permitAll()
                .pathMatchers("/logout").permitAll()  // Allow unauthenticated access to logout
                .anyExchange().authenticated())
            .exceptionHandling(exceptionHandling -> 
                exceptionHandling.authenticationEntryPoint(authenticationEntryPoint))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
            .oauth2Login(oauth2 -> {})
            .oauth2Client(oauth2 -> {})
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")  // Explicitly set logout URL
                .requiresLogout(ServerWebExchangeMatchers.pathMatchers("/logout"))  // Accept any HTTP method
                .logoutSuccessHandler(oidcLogoutSuccessHandler)
            );
        
        return http.build();
    }

    /**
     * Custom authentication entry point that handles AJAX and browser requests differently.
     * AJAX requests (identified by Accept: application/json header) receive a 401 status,
     * while browser requests are redirected to the OAuth2 login page.
     */
    @Bean
    public ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            var request = exchange.getRequest();
            var acceptHeader = request.getHeaders().getAccept();
            
            // Check if this is an AJAX request
            boolean isAjaxRequest = acceptHeader.stream()
                    .anyMatch(mediaType -> mediaType.includes(MediaType.APPLICATION_JSON));
            
            if (isAjaxRequest) {
                // Return 401 for AJAX requests
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            } else {
                // Redirect to OAuth2 login for browser requests
                return new RedirectServerAuthenticationEntryPoint("/oauth2/authorization/keycloak")
                        .commence(exchange, ex);
            }
        };
    }

    /**
     * Configure OIDC logout to properly end the SSO session at Keycloak.
     * This handler redirects to Keycloak's end_session_endpoint, which:
     * 1. Terminates the Keycloak session
     * 2. Logs out from all SSO-connected applications
     * 3. Redirects back to the post-logout redirect URI
     * 
     * Without this, users remain logged into Keycloak even after logout.
     */
    @Bean
    public ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
            ReactiveClientRegistrationRepository clientRegistrationRepository) {
        
        OidcClientInitiatedServerLogoutSuccessHandler successHandler = 
            new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        
        // Set the URI to redirect to after Keycloak completes logout
        // If not set, defaults to the application's base URL
        successHandler.setPostLogoutRedirectUri("{baseUrl}");
        
        return successHandler;
    }

    // @Bean
    // @ConditionalOnProperty(prefix = "patternslib.gateway.security", name = "enabled", havingValue = "false")
    // public SecurityWebFilterChain permitAllSecurityWebFilterChain(ServerHttpSecurity http) {
    //     http
    //             .csrf(csrf -> csrf.disable())
    //             .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
    //     return http.build();
    // }
}

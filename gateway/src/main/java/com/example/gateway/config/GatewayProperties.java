package com.example.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "patternslib.gateway")
public class GatewayProperties {

    @NestedConfigurationProperty
    private Security security = new Security();

    @NestedConfigurationProperty
    private RateLimit rateLimit = new RateLimit();

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public void setRateLimit(RateLimit rateLimit) {
        this.rateLimit = rateLimit;
    }

    /** Controls the centralized JWT authentication filter chain. */
    public static class Security {

        /**
         * Master switch for {@code SecurityConfig}. Disable only for local
         * development or when authentication is handled by an upstream
         * load balancer / service mesh instead.
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /** Fallback values used by the RateLimit route filter when a route doesn't specify its own args. */
    public static class RateLimit {

        private int defaultLimit = 100;
        private int defaultWindowSeconds = 60;

        public int getDefaultLimit() {
            return defaultLimit;
        }

        public void setDefaultLimit(int defaultLimit) {
            this.defaultLimit = defaultLimit;
        }

        public int getDefaultWindowSeconds() {
            return defaultWindowSeconds;
        }

        public void setDefaultWindowSeconds(int defaultWindowSeconds) {
            this.defaultWindowSeconds = defaultWindowSeconds;
        }
    }
}

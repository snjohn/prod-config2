package com.example.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.data.redis.config.annotation.web.server.EnableRedisWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@Configuration
@EnableRedisWebSession
public class SessionConfig {

    @Bean
    public WebSessionIdResolver webSessionIdResolver() {
        CookieWebSessionIdResolver resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("GATEWAY_SESSION");
        resolver.addCookieInitializer(cookie -> {
            cookie.path("/");
            cookie.httpOnly(true);
            cookie.secure(false); // TODO: Set to true in production with HTTPS
            cookie.sameSite("Lax");
        });
        return resolver;
    }
}

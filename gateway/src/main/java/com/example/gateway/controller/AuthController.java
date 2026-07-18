package com.example.gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller that provides endpoints for checking authentication status
 * and retrieving user information.
 */
@RestController
public class AuthController {

    /**
     * Check if the user is authenticated and return basic user info.
     * This endpoint is called by the frontend on initial page load to verify authentication.
     * If the user is not authenticated, Spring Security will redirect to the OAuth2 login.
     * 
     * @param principal The authenticated OIDC user
     * @return User information including name and email
     */
    @GetMapping("/auth/check")
    public ResponseEntity<Map<String, Object>> checkAuth(@AuthenticationPrincipal OidcUser principal) {
        Map<String, Object> response = new HashMap<>();
        
        if (principal != null) {
            response.put("authenticated", true);
            response.put("name", principal.getFullName());
            response.put("email", principal.getEmail());
            response.put("username", principal.getPreferredUsername());
        } else {
            response.put("authenticated", false);
        }
        
        return ResponseEntity.ok(response);
    }
}

package com.example.backend.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    // The @AuthenticationPrincipal annotation gives you access to the 
    // validated JWT claims directly in your method.
    @GetMapping("/{id}")
    public Map<String, Object> getOrderDetails(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        
        // You can extract information about the user from the JWT
        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("preferred_username");
        
        // Business logic here...
        return Map.of(
            "orderId", id,
            "message", "Order fetched successfully",
            "requestedBy", username
        );
    }
}
# Rate Limiting Configuration Guide

This document explains the rate limiting options available in the gateway and how to choose the right strategy for your deployment.

## Overview

The gateway supports two rate limiting implementations:

1. **In-Memory Rate Limiter** (default) - Simple, single-instance solution
2. **Redis-Based Rate Limiter** (production recommended) - Distributed, multi-instance solution

## In-Memory Rate Limiter

### When to Use
- **Single gateway instance** deployments
- Development and testing environments
- Simple applications with low traffic
- When you want minimal dependencies

### Limitations
- ⚠️ **Not distributed**: Each gateway instance maintains independent counters
- ⚠️ **Memory consumption**: Stores all client rate limit windows in memory
- ⚠️ **Lost on restart**: Rate limit state is not persisted

### Configuration

The in-memory rate limiter is already configured and includes automatic cleanup to prevent memory leaks:

```yaml
patternslib:
  gateway:
    rate-limit:
      default-limit: 100          # requests per window
      default-window-seconds: 60  # window duration
```

Apply to routes:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: backend-service
          uri: lb://backend
          predicates:
            - Path=/api/**
          filters:
            - name: RateLimit
              args:
                limit: 100
                windowSeconds: 60
```

### Features
✅ **Automatic cleanup**: Expired windows are removed every 5 minutes  
✅ **Null-safe**: Handles missing client IP addresses gracefully  
✅ **Per-IP limiting**: Rate limits based on client IP address  

## Redis-Based Rate Limiter (Production Recommended)

### When to Use
- **Multiple gateway instances** (horizontal scaling)
- Production environments
- When you need consistent rate limits across all instances
- When you need persistent rate limit state

### Advantages
✅ **Distributed**: All gateway instances share the same Redis state  
✅ **Accurate**: True rate limiting across your entire gateway cluster  
✅ **Flexible**: Multiple key resolver strategies (IP, user, API key, combined)  
✅ **Token bucket algorithm**: Smoother rate limiting with burst capacity  

### Setup Instructions

#### 1. Ensure Redis is Available
Redis is already configured in your `docker-compose.yml`:

```yaml
services:
  redis:
    image: redis:alpine
    container_name: redis
    ports:
      - "6379:6379"
```

#### 2. Choose a Key Resolver Strategy

The gateway provides several key resolver beans in `RateLimitKeyResolverConfig.java`:

- **`remoteAddrKeyResolver`** - Rate limit by client IP (best for anonymous traffic)
- **`principalNameKeyResolver`** - Rate limit by authenticated user (best for API quotas)
- **`apiKeyResolver`** - Rate limit by API key header
- **`combinedKeyResolver`** - User if authenticated, otherwise IP

#### 3. Update Route Configuration

Replace the `RateLimit` filter with `RequestRateLimiter`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: backend-service
          uri: lb://backend
          predicates:
            - Path=/api/**
          filters:
            - TokenRelay
            - name: RequestRateLimiter
              args:
                # Steady-state rate: 10 requests per second
                redis-rate-limiter.replenishRate: 10
                # Maximum burst: 20 requests
                redis-rate-limiter.burstCapacity: 20
                # Tokens per request (default: 1)
                redis-rate-limiter.requestedTokens: 1
                # Which strategy to use
                key-resolver: "#{@remoteAddrKeyResolver}"
```

#### 4. Example: Different Limits for Different Routes

```yaml
spring:
  cloud:
    gateway:
      routes:
        # High-traffic public API
        - id: public-api
          uri: lb://public-service
          predicates:
            - Path=/api/public/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
                key-resolver: "#{@remoteAddrKeyResolver}"
        
        # User-specific API (authenticated)
        - id: user-api
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - TokenRelay
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 5
                redis-rate-limiter.burstCapacity: 10
                key-resolver: "#{@principalNameKeyResolver}"
```

## Understanding Redis Rate Limiter Parameters

### replenishRate
The number of tokens added to the bucket per second. This is your **steady-state rate**.

Example: `replenishRate: 10` = 10 requests per second sustained

### burstCapacity
The maximum number of tokens the bucket can hold. This allows for **burst traffic**.

Example: `burstCapacity: 20` = Client can make 20 requests immediately, then limited to replenishRate

### requestedTokens
Number of tokens consumed per request. Usually `1`, but can be higher for expensive operations.

Example: `requestedTokens: 5` = Each request consumes 5 tokens (effectively 5x rate limit)

## Migration Path

### Phase 1: Development (Current)
Use in-memory rate limiter with automatic cleanup ✅

### Phase 2: Production Preparation
1. Review the example configuration in `application-production-ratelimit.yaml.example`
2. Test Redis-based rate limiting in staging
3. Choose appropriate key resolver strategies for your routes

### Phase 3: Production Deployment
1. Replace `RateLimit` filters with `RequestRateLimiter` in your production config
2. Set appropriate `replenishRate` and `burstCapacity` values
3. Monitor Redis performance
4. Consider removing the in-memory `RateLimitGatewayFilterFactory` if no longer needed

## Monitoring

Both rate limiters return HTTP 429 (Too Many Requests) when limits are exceeded.

Monitor these metrics:
- Number of 429 responses (adjust limits if too high)
- Redis connection health (for Redis-based limiter)
- Memory usage (for in-memory limiter)

## Summary

| Feature | In-Memory | Redis-Based |
|---------|-----------|-------------|
| Multiple instances | ❌ No | ✅ Yes |
| Production ready | ⚠️ Single instance only | ✅ Yes |
| Setup complexity | ✅ Simple | ⚠️ Moderate |
| Memory efficient | ⚠️ With cleanup | ✅ Yes |
| Persistent | ❌ No | ✅ Yes |
| Flexible strategies | ❌ IP only | ✅ IP, User, API Key, Combined |
| **Recommended for** | **Development/Testing** | **Production** |

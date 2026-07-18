# Health Monitoring Guide

This document explains the custom health indicators implemented in the gateway and how to use them for monitoring and operations.

## Overview

The gateway includes custom health indicators that provide comprehensive visibility into the health of critical dependencies:

1. **Keycloak Health Indicator** - OAuth2/OIDC provider connectivity
2. **Redis Session Health Indicator** - Session storage connectivity  
3. **Eureka Health Indicator** - Service discovery connectivity

These health indicators are exposed via Spring Boot Actuator's `/actuator/health` endpoint.

## Accessing Health Information

### Basic Health Check
```bash
curl http://localhost:8081/actuator/health
```

### Detailed Health Check (with component details)
The detailed view shows individual health indicators:

```bash
curl http://localhost:8081/actuator/health
```

Example response:
```json
{
  "status": "UP",
  "components": {
    "keycloak": {
      "status": "UP",
      "details": {
        "issuerUri": "http://keycloak:8080/realms/myrealm",
        "status": "Keycloak is reachable"
      }
    },
    "redisSession": {
      "status": "UP",
      "details": {
        "version": "7.2.0",
        "purpose": "Session storage",
        "status": "Redis is reachable"
      }
    },
    "eureka": {
      "status": "UP",
      "details": {
        "services": 2,
        "registeredServices": ["backend", "gateway"],
        "status": "Connected to Eureka"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

## Health Indicators Explained

### 1. Keycloak Health Indicator

**Component Name**: `keycloak`  
**File**: `KeycloakHealthIndicator.java`

**What it checks**:
- Calls Keycloak's OIDC configuration endpoint (`/.well-known/openid-configuration`)
- Verifies Keycloak is reachable and responding
- Times out after 3 seconds

**Status meanings**:
- **UP**: Keycloak is reachable and responding to OIDC configuration requests
- **DOWN**: Keycloak is unreachable, timed out, or returned an error

**Details provided**:
- `issuerUri`: The configured Keycloak issuer URI
- `status`: Human-readable status message
- `error`: Error type if DOWN (e.g., "WebClientRequestException")
- `message`: Error message if DOWN

**Why it matters**:
If Keycloak is down, users cannot log in and JWT validation will fail.

### 2. Redis Session Health Indicator

**Component Name**: `redisSession`  
**File**: `RedisSessionHealthIndicator.java`

**What it checks**:
- Connects to Redis server
- Retrieves server info to confirm connectivity
- Extracts Redis version
- Times out after 3 seconds

**Status meanings**:
- **UP**: Redis is reachable and session storage is functional
- **DOWN**: Redis is unreachable or connection failed

**Details provided**:
- `version`: Redis server version
- `purpose`: "Session storage" (to distinguish from other Redis uses)
- `status`: Human-readable status message
- `error`: Error type if DOWN
- `message`: Error message if DOWN

**Why it matters**:
If Redis is down, user sessions cannot be stored/retrieved, breaking authentication persistence across requests.

### 3. Eureka Health Indicator

**Component Name**: `eureka`  
**File**: `EurekaHealthIndicator.java`

**What it checks**:
- Queries the Eureka discovery client for registered services
- Counts number of available services
- Lists service names

**Status meanings**:
- **UP**: Connected to Eureka and can retrieve service registry
- **DOWN**: Cannot connect to Eureka or retrieve services

**Details provided**:
- `services`: Number of registered services
- `registeredServices`: List of service names
- `status`: Human-readable status message
- `error`: Error type if DOWN
- `message`: Error message if DOWN

**Why it matters**:
If Eureka is down, the gateway cannot discover backend services for load balancing and routing.

## Configuration

### Enable/Disable Health Details

By default, detailed health information is shown. This is configured in `application.yaml`:

```yaml
management:
  endpoint:
    health:
      show-details: always  # Options: always, when-authorized, never
```

### Security Considerations

The health endpoint is currently public (no authentication required):

```java
.pathMatchers("/actuator/health/**", "/actuator/info").permitAll()
```

**Production recommendation**: Consider restricting detailed health information to authenticated admin users only:

```yaml
management:
  endpoint:
    health:
      show-details: when-authorized
      roles: ADMIN
```

## Using Health Checks in Operations

### Kubernetes/Container Orchestration

The health endpoint is used in the Docker Compose healthcheck:

```yaml
healthcheck:
  test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://gateway:8080/actuator/health"]
  interval: 10s
  timeout: 5s
  retries: 5
  start_period: 40s
```

For Kubernetes, use liveness and readiness probes:

```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 40
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  initialDelaySeconds: 10
  periodSeconds: 5
```

### Monitoring and Alerting

Configure your monitoring system (Prometheus, Datadog, New Relic, etc.) to:

1. **Poll the health endpoint** regularly
2. **Alert on DOWN status** for any component
3. **Track component availability** over time
4. **Set up dependencies**:
   - Keycloak DOWN → Authentication issues
   - Redis DOWN → Session storage issues
   - Eureka DOWN → Service discovery issues

### Load Balancer Health Checks

Configure load balancers to use the health endpoint:

```
Health Check URL: /actuator/health
Expected Status: 200 OK
Check Interval: 10 seconds
Timeout: 5 seconds
Unhealthy Threshold: 3 consecutive failures
```

## Troubleshooting

### All Health Checks Failing

**Symptom**: All custom health indicators show DOWN  
**Likely Cause**: Gateway cannot connect to any dependencies  
**Actions**:
1. Check network connectivity from gateway container
2. Verify all dependency containers are running
3. Check Docker network configuration
4. Review gateway logs for connection errors

### Keycloak Health Check Failing

**Symptom**: `keycloak` component shows DOWN  
**Likely Causes**:
- Keycloak container is not running
- Incorrect issuer URI configuration
- Network connectivity issue
- Keycloak is starting up (check start_period)

**Actions**:
```bash
# Check Keycloak is running
docker ps | grep keycloak

# Test Keycloak connectivity manually
curl http://keycloak:8080/realms/myrealm/.well-known/openid-configuration

# Check configuration
grep issuer-uri gateway/src/main/resources/application.yaml
```

### Redis Health Check Failing

**Symptom**: `redisSession` component shows DOWN  
**Likely Causes**:
- Redis container is not running
- Redis password mismatch
- Network connectivity issue

**Actions**:
```bash
# Check Redis is running
docker ps | grep redis

# Test Redis connectivity
docker exec redis redis-cli ping
# Should return: PONG

# Check Redis configuration
grep redis gateway/src/main/resources/application.yaml
```

### Eureka Health Check Failing

**Symptom**: `eureka` component shows DOWN  
**Likely Causes**:
- Eureka server is not running
- Gateway not registered with Eureka
- Network connectivity issue

**Actions**:
```bash
# Check Eureka is running
docker ps | grep eureka

# Access Eureka dashboard
open http://localhost:8761

# Check Eureka configuration
grep eureka gateway/src/main/resources/application.yaml
```

## Adding Custom Health Indicators

To add additional health indicators for new dependencies:

1. Create a new class implementing `ReactiveHealthIndicator`
2. Annotate with `@Component`
3. Implement the `health()` method returning `Mono<Health>`
4. Return appropriate UP/DOWN status with details

Example:
```java
@Component
public class DatabaseHealthIndicator implements ReactiveHealthIndicator {
    
    @Override
    public Mono<Health> health() {
        return checkDatabaseConnection()
            .map(connected -> {
                if (connected) {
                    return Health.up()
                        .withDetail("database", "connected")
                        .build();
                } else {
                    return Health.down()
                        .withDetail("database", "disconnected")
                        .build();
                }
            });
    }
}
```

## Summary

The custom health indicators provide comprehensive visibility into gateway dependencies:

| Indicator | Checks | Timeout | Critical? |
|-----------|--------|---------|-----------|
| Keycloak | OIDC endpoint | 3s | Yes - Authentication required |
| Redis Session | Connection + info | 3s | Yes - Session storage required |
| Eureka | Service registry | N/A | Yes - Service discovery required |

Monitor these health indicators in production to ensure gateway reliability and quick incident response.

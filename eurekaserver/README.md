# Eureka Service Discovery Server

## Overview

Netflix Eureka server for service discovery and registration. All microservices (gateway, backend) register themselves with Eureka for dynamic service discovery.

## Access

- **Eureka Dashboard**: http://localhost:8761
- **Eureka REST API**: http://localhost:8761/eureka/apps

## Registered Services

The following services register with Eureka:
- **gateway** - Spring Cloud Gateway (OAuth2 client)
- **backend** - Backend microservice (OAuth2 resource server)

## Configuration

### Server Configuration
- Port: 8761
- Self-preservation: Disabled (dev mode)
- Eviction interval: 5 seconds

### Client Registration
Services register with Eureka using:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://eurekaserver:8761/eureka/
```

## Usage in Gateway

The gateway uses Eureka for service discovery with the `lb://` protocol:
```yaml
uri: lb://backend  # Resolves to registered backend instances
```

## Docker Compose

Eureka server is available at `eurekaserver:8761` within the Docker network.

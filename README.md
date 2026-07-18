# Multi-Module Maven Project Structure

This is a multi-module Maven project containing microservices with Eureka service discovery, Spring Cloud Gateway, and a backend service.

## Project Structure

```
prod-config/
├── pom.xml                    # Parent POM
├── docker-compose.yml         # Docker orchestration
├── backend/                   # Backend microservice
│   ├── pom.xml
│   └── src/
├── eurekaserver/             # Service discovery server
│   ├── pom.xml
│   └── src/
├── gateway/                   # API Gateway with OAuth2
│   ├── pom.xml
│   └── src/
├── frontend/                  # Next.js React application
├── keycloak/                  # Keycloak realm configuration
└── nginx/                     # NGINX configuration
```

## Modules

### 1. **eurekaserver** (Port 8761)
Netflix Eureka service discovery server where all microservices register.

### 2. **gateway** (Port 8080)
Spring Cloud Gateway with OAuth2/OIDC authentication via Keycloak.

### 3. **backend**
Backend microservice that provides REST APIs, secured with JWT validation.

## Building the Project

### Enable BuildKit (Recommended)
BuildKit is enabled via the `.env` file for faster builds and dependency caching:
```bash
# Already configured in .env
export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1
```

### Build All Modules
```bash
mvn clean install
```

### Build Specific Module
```bash
mvn clean install -pl backend
mvn clean install -pl gateway
mvn clean install -pl eurekaserver
```

### Skip Tests
```bash
mvn clean install -DskipTests
```

## Running with Docker Compose

**Note:** Maven dependencies are cached using Docker BuildKit. The first build will download all dependencies, but subsequent builds will reuse the cache, making builds much faster.

Build and start all services:
```bash
docker-compose up --build
```

Start services in detached mode:
```bash
docker-compose up -d
```

Stop all services:
```bash
docker-compose down
```

## Service Endpoints

- **Eureka Dashboard**: http://localhost:8761
- **Keycloak Admin**: http://localhost:8080 (admin/admin)
- **Gateway**: http://localhost (proxied via NGINX)
- **Frontend**: http://localhost

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.15
- **Spring Cloud**: 2024.0.1
- **Maven**: Multi-module project
- **Docker**: Containerization
- **Keycloak**: Identity and access management
- **NGINX**: Reverse proxy and static file server
- **Next.js**: React framework for frontend

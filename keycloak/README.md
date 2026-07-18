# Keycloak Configuration

## Realm Import

This directory contains the Keycloak realm configuration that is automatically imported on startup.

### Realm: `myrealm`

**Clients:**
- **gateway** (client-id: `gateway`, secret: `secret`)
  - OAuth2/OIDC client for Spring Cloud Gateway
  - Redirect URIs: `http://localhost/*`, `http://yourdomain.com/*`

**Test Users:**
- **testuser** / password: `password` (role: user)
- **admin** / password: `admin` (roles: admin, user)

### Access Keycloak Admin Console

- URL: http://localhost:8080
- Username: `admin`
- Password: `admin`

### Realm Import

The realm is automatically imported when Keycloak starts via the `--import-realm` flag.
All realm configurations are stored in the `realms/` directory.

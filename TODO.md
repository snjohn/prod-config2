# TODO for Production


## Gateway

### 1. **Insecure Cookie Configuration** (SessionConfig.java:17)
cookie.secure(false) allows cookies over HTTP (man-in-the-middle risk)
Should be environment-based: secure(${COOKIE_SECURE:true})

### 2. **Missing Resilience Patterns**
No circuit breakers (Resilience4j)
No retry logic for backend services
No timeout configurations on routes
Gateway will fail if backend services are slow/down


import axios from 'axios';

// When this hits the Gateway, the Gateway checks for a session.
// If no session, the Gateway (configured as OIDC client) will 
// redirect the browser to Keycloak.
const api = axios.create({
    baseURL: '/api',
    withCredentials: true, // Essential for sending the session cookie
    headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    }
});

// Add response interceptor to handle authentication redirects
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // If we get a 401, redirect to OAuth2 authorization to trigger login
        if (error.response?.status === 401) {
            // Redirect to OAuth2 authorization to trigger login
            window.location.href = '/oauth2/authorization/keycloak';
            return new Promise(() => {}); // Prevent further error handling
        }
        return Promise.reject(error);
    }
);

export default api;
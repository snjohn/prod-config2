/**
 * Logout from both the Gateway session and Keycloak SSO session
 * 
 * This function:
 * 1. Clears the Spring Security session in the gateway
 * 2. Redirects to Keycloak's logout endpoint to clear the SSO session
 * 3. Keycloak then redirects back to the application
 */
export async function logout(): Promise<void> {
  try {
    // First, logout from the gateway (clears Spring Security session)
    await fetch('/logout', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
    });
  } catch (error) {
    console.error('Error during gateway logout:', error);
    // Continue to Keycloak logout even if gateway logout fails
  }

  // Redirect to Keycloak logout endpoint
  const keycloakBaseUrl = 'http://keycloak:8080';
  const realm = 'myrealm';
  const clientId = 'gateway';
  const redirectUri = window.location.origin;

  const logoutUrl = 
    `${keycloakBaseUrl}/realms/${realm}/protocol/openid-connect/logout` +
    `?post_logout_redirect_uri=${encodeURIComponent(redirectUri)}` +
    `&client_id=${clientId}`;

  window.location.href = logoutUrl;
}

/**
 * Check if the user is authenticated by testing the gateway session
 */
export async function checkAuth(): Promise<boolean> {
  try {
    const response = await fetch('/api/health', {
      credentials: 'include',
    });
    return response.ok;
  } catch (error) {
    return false;
  }
}

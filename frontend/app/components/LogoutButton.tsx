'use client';

import { useState } from 'react';

export default function LogoutButton() {
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  const handleLogout = () => {
    setIsLoggingOut(true);
    
    // Redirect to the gateway's logout endpoint
    // The gateway's OidcClientInitiatedServerLogoutSuccessHandler will:
    // 1. Clear the gateway session
    // 2. Redirect to Keycloak's end_session_endpoint
    // 3. Keycloak will clear the SSO session
    // 4. Keycloak will redirect back to the app (post_logout_redirect_uri)
    window.location.href = '/logout';
  };

  return (
    <button
      onClick={handleLogout}
      disabled={isLoggingOut}
      className="flex h-12 items-center justify-center rounded-full bg-red-600 px-6 text-white font-medium transition-colors hover:bg-red-700 disabled:bg-red-400 disabled:cursor-not-allowed"
    >
      {isLoggingOut ? 'Logging out...' : 'Logout'}
    </button>
  );
}

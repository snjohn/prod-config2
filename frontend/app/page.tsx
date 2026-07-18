'use client';

import { useState, useEffect } from 'react';
import Image from "next/image";
import api from './api';

interface Order {
  orderId: string;
  message: string;
  requestedBy: string;
}

interface AuthInfo {
  authenticated: boolean;
  name?: string;
  email?: string;
  username?: string;
}

export default function Home() {
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [authInfo, setAuthInfo] = useState<AuthInfo | null>(null);
  const [checkingAuth, setCheckingAuth] = useState(true);

  // Check authentication on page load
  useEffect(() => {
    const checkAuth = async () => {
      try {
        const response = await fetch('/auth/check', {
          credentials: 'include',
          headers: {
            'Accept': 'application/json'
          }
        });
        
        if (response.ok) {
          const data = await response.json();
          if (data.authenticated) {
            setAuthInfo(data);
            setCheckingAuth(false);
          } else {
            // Not authenticated, redirect to login
            window.location.href = '/oauth2/authorization/keycloak';
          }
        } else if (response.status === 401 || response.status === 302) {
          // Redirect to login
          window.location.href = '/oauth2/authorization/keycloak';
        }
      } catch (err) {
        console.error('Auth check failed:', err);
        // On error, try to redirect to login
        window.location.href = '/oauth2/authorization/keycloak';
      }
    };

    checkAuth();
  }, []);

  const fetchOrder = async () => {
    setLoading(true);
    setError(null);
    setOrder(null);
    
    try {
      const response = await api.get('/orders/123');
      setOrder(response.data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch order');
    } finally {
      setLoading(false);
    }
  };

  // Show loading while checking authentication
  if (checkingAuth) {
    return (
      <div className="flex flex-col flex-1 items-center justify-center bg-zinc-50 dark:bg-black">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-zinc-600 dark:text-zinc-400">Checking authentication...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col flex-1 items-center bg-zinc-50 dark:bg-black p-8">
      <main className="flex flex-col w-full max-w-4xl gap-8">
        <div className="flex items-center gap-4">
          <Image
            className="dark:invert"
            src="/next.svg"
            alt="Next.js logo"
            width={100}
            height={20}
            priority
          />
          <div className="flex-1">
            <h1 className="text-2xl font-semibold text-black dark:text-zinc-50">
              Welcome to the Microservices App
            </h1>
            {authInfo && (
              <p className="text-sm text-zinc-600 dark:text-zinc-400 mt-1">
                Logged in as: <span className="font-medium">{authInfo.name || authInfo.username}</span>
              </p>
            )}
          </div>
        </div>

        <div className="flex flex-col gap-6 bg-white dark:bg-zinc-900 p-6 rounded-lg border border-zinc-200 dark:border-zinc-800">
          <h2 className="text-xl font-semibold text-black dark:text-zinc-50">
            Test Backend API
          </h2>
          <p className="text-zinc-600 dark:text-zinc-400">
            Click the button below to fetch order #123 from the backend service.
          </p>
          
          <button
            onClick={fetchOrder}
            disabled={loading}
            className="flex h-12 w-fit items-center justify-center rounded-lg bg-blue-600 px-6 text-white font-medium transition-colors hover:bg-blue-700 disabled:bg-blue-400 disabled:cursor-not-allowed"
          >
            {loading ? 'Loading...' : 'Fetch Order #123'}
          </button>

          {/* Results Section */}
          {error && (
            <div className="p-4 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 rounded-lg">
              <p className="text-red-600 dark:text-red-400 font-medium">Error:</p>
              <p className="text-red-800 dark:text-red-300">{error}</p>
            </div>
          )}

          {order && (
            <div className="p-4 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg">
              <p className="text-green-600 dark:text-green-400 font-medium mb-3">Order Details:</p>
              <div className="space-y-2 text-zinc-800 dark:text-zinc-200">
                <p><span className="font-semibold">Order ID:</span> {order.orderId}</p>
                <p><span className="font-semibold">Message:</span> {order.message}</p>
                <p><span className="font-semibold">Requested By:</span> {order.requestedBy}</p>
              </div>
            </div>
          )}
        </div>

        <div className="p-4 bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg">
          <p className="text-sm text-blue-800 dark:text-blue-200">
            <span className="font-semibold">ℹ️ Info:</span> Your session is stored in Redis. Use the logout button in the header to end your session.
          </p>
        </div>
      </main>
    </div>
  );
}

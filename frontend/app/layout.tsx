import type { Metadata } from "next";
import "./globals.css";
import LogoutButton from "./components/LogoutButton";

export const metadata: Metadata = {
  title: "Microservices App",
  description: "Spring Cloud Gateway with OAuth2 and Keycloak",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className="h-full antialiased">
      <body className="min-h-full flex flex-col font-sans">
        <header className="bg-white dark:bg-zinc-900 border-b border-zinc-200 dark:border-zinc-800">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
            <h1 className="text-xl font-semibold text-zinc-900 dark:text-zinc-100">
              Microservices App
            </h1>
            <LogoutButton />
          </div>
        </header>
        {children}
      </body>
    </html>
  );
}

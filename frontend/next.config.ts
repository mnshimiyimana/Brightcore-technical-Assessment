import type { NextConfig } from "next";

// Where the Spring Boot API runs. Read at build time for the rewrite below.
const backendUrl = process.env.BACKEND_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  output: "standalone",
  poweredByHeader: false,
  // The browser only ever talks to this Next.js server; /api/* is proxied to Spring Boot.
  // Same origin means no CORS setup and the HttpOnly auth cookie just works.
  async rewrites() {
    return [{ source: "/api/:path*", destination: `${backendUrl}/api/:path*` }];
  },
};

export default nextConfig;

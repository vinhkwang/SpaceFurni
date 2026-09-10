import type { NextConfig } from "next";

const LOOPBACK_HOSTNAMES = ["localhost", "127.0.0.1", "::1"];

function isLoopbackHostname(hostname: string): boolean {
  return LOOPBACK_HOSTNAMES.includes(hostname);
}

function productImagesConfig(): NextConfig["images"] {
  const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_URL;
  if (!apiBaseUrl) {
    return { remotePatterns: [] };
  }
  const parsedApiBaseUrl = new URL(apiBaseUrl);
  return {
    remotePatterns: [
      {
        protocol: parsedApiBaseUrl.protocol.replace(":", "") as "http" | "https",
        hostname: parsedApiBaseUrl.hostname,
        port: parsedApiBaseUrl.port,
        pathname: "/uploads/**",
      },
    ],
    dangerouslyAllowLocalIP: isLoopbackHostname(parsedApiBaseUrl.hostname),
  };
}

const nextConfig: NextConfig = {
  images: productImagesConfig(),
};

export default nextConfig;

import "server-only";

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

/**
 * Server-side fetch used by public pages (rendered on the server for speed and SEO).
 * Returns null on 404 so pages can call notFound().
 */
export async function serverFetch<T>(path: string): Promise<T | null> {
  const res = await fetch(`${BACKEND_URL}${path}`, { cache: "no-store" });
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`API ${path} failed with ${res.status}`);
  return (await res.json()) as T;
}

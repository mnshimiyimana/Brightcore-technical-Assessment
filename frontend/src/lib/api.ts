import type { ApiErrorBody } from "./types";

/** An error response from the API, keeping per-field messages for forms. */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
    public readonly fieldErrors: Record<string, string> = {},
  ) {
    super(message);
  }
}

/**
 * Browser-side fetch to our own /api proxy. The HttpOnly auth cookie is sent automatically;
 * a 401 on an admin call sends the user to the login page.
 */
export async function apiFetch<T>(path: string, init: RequestInit = {}): Promise<T> {
  const isForm = init.body instanceof FormData;
  const res = await fetch(path, {
    ...init,
    credentials: "same-origin",
    headers: {
      Accept: "application/json",
      ...(init.body && !isForm ? { "Content-Type": "application/json" } : {}),
      ...init.headers,
    },
  });

  if (res.status === 401 && path.startsWith("/api/admin") && typeof window !== "undefined") {
    window.location.href = `/admin/login?next=${encodeURIComponent(window.location.pathname)}`;
  }

  if (!res.ok) {
    let body: Partial<ApiErrorBody> = {};
    try {
      body = await res.json();
    } catch {
      // Non-JSON error (e.g. proxy failure); fall back to a generic message.
    }
    throw new ApiError(res.status, body.message ?? "Something went wrong. Please try again.", body.errors ?? {});
  }

  return res.status === 204 ? (undefined as T) : ((await res.json()) as T);
}

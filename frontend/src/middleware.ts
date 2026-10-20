import { NextResponse, type NextRequest } from "next/server";

/**
 * Sends visitors without an auth cookie to the login page before any admin page renders.
 * This is a convenience only: the real check happens in the Spring Boot API, which verifies
 * the token on every /api/admin request.
 */
export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;
  if (pathname === "/admin/login" || request.cookies.has("admin_token")) {
    return NextResponse.next();
  }
  const loginUrl = new URL("/admin/login", request.url);
  loginUrl.searchParams.set("next", pathname);
  return NextResponse.redirect(loginUrl);
}

export const config = { matcher: ["/admin/:path*"] };

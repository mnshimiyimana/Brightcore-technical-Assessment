"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { apiFetch } from "@/lib/api";
import { Logo } from "@/components/Logo";

const NAV = [
  { href: "/admin", label: "Overview" },
  { href: "/admin/jobs", label: "Jobs" },
  { href: "/admin/jobs/new", label: "Post a job" },
];

export default function AdminLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const [menuOpen, setMenuOpen] = useState(false);

  const isActive = (href: string) =>
    href === "/admin" ? pathname === "/admin" : href === "/admin/jobs" ? pathname.startsWith("/admin/jobs") && pathname !== "/admin/jobs/new" : pathname === href;

  async function logout() {
    await apiFetch("/api/auth/logout", { method: "POST" }).catch(() => undefined);
    router.replace("/admin/login");
    router.refresh();
  }

  return (
    <div className="min-h-screen">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <Logo href="/admin" suffix="Admin" />
          <button
            type="button"
            className="btn-secondary px-3 sm:hidden"
            onClick={() => setMenuOpen((open) => !open)}
            aria-expanded={menuOpen}
            aria-controls="admin-nav"
          >
            Menu
          </button>
          <nav
            id="admin-nav"
            className={`${menuOpen ? "flex" : "hidden"} absolute inset-x-0 top-14 z-10 flex-col gap-1 border-b border-slate-200 bg-white p-3 shadow-sm sm:static sm:flex sm:flex-row sm:items-center sm:border-0 sm:p-0 sm:shadow-none`}
          >
            {NAV.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setMenuOpen(false)}
                className={`rounded-lg px-3 py-2 text-sm font-medium ${isActive(item.href) ? "bg-brand-50 text-brand-700" : "text-slate-600 hover:bg-slate-100"}`}
              >
                {item.label}
              </Link>
            ))}
            <Link href="/jobs" className="rounded-lg px-3 py-2 text-sm text-slate-500 hover:bg-slate-100" target="_blank">
              View site ↗
            </Link>
            <button type="button" onClick={logout} className="rounded-lg px-3 py-2 text-left text-sm font-medium text-red-600 hover:bg-red-50">
              Log out
            </button>
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8">{children}</main>
    </div>
  );
}
